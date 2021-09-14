package com.vrv.framework.client.invocation;

import com.vrv.framework.client.router.ServiceRouter;
import com.vrv.framework.client.model.VoaTransport;
import com.vrv.framework.client.definition.ClassDefinition;
import com.vrv.framework.client.definition.MethodDefinition;
import com.vrv.framework.client.filter.NodeFilter;
import com.vrv.framework.client.model.Node;
import com.vrv.framework.common.exception.VoaRuntimeException;
import com.vrv.framework.common.exception.VoaTransportException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * proxy的CemsInvocationHandler
 *
 * @author chenlong
 */
public class CemsServiceInvocationHandler implements InvocationHandler {

    private static final String ENCODING_DEFAULT = "UTF-8";
    static Logger logger = LoggerFactory.getLogger(CemsServiceInvocationHandler.class);

    private ClassDefinition serviceDefinition;

    private ConcurrentMap<Method, MethodDefinition> methodCache = new ConcurrentHashMap<Method, MethodDefinition>();

    private ServiceRouter serviceRouter;

    private NodeFilter filter;

    private long timeOut;

    public ClassDefinition getServiceDefinition() {

        return serviceDefinition;
    }

    public ConcurrentMap<Method, MethodDefinition> getMethodCache() {

        return methodCache;
    }

    public ServiceRouter getServiceRouter() {

        return serviceRouter;
    }

    public CemsServiceInvocationHandler(ServiceRouter serviceRouter, ClassDefinition serviceDefinition, long timeOut, NodeFilter filter) {

        if (serviceDefinition == null || serviceRouter == null) {
            throw new NullPointerException();
        }
        this.serviceRouter = serviceRouter;
        this.serviceDefinition = serviceDefinition;
        this.timeOut = timeOut;
        this.filter = filter;
    }

    @Override
    public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        return doInvoke(proxy, method, args);
    }

    protected final Object doInvoke(Object proxy, Method method, Object[] args) throws VoaRuntimeException, Exception {

        // Service返回的结果
        Object result = null;
        try {
            result = doInvokeLogic(proxy, method, args);
        } catch (Exception e) {
            // 如果是网络异常，并重连一次。
            Throwable cause = e.getCause();
            logger.info("Throwable:" + cause.getClass().getName());
            if (cause instanceof TTransportException) {
                if (cause instanceof SocketTimeoutException) {
                    throw e;
                }
                // 其他的异常，再回调重试一次
                logger.info("网络处理异常,开始重连一次");
                result = doInvokeLogic(proxy, method, args);
            } else {
                throw e;
            }
        }
        return result;
    }

    /**
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws VoaRuntimeException 框架抛出的异常
     * @throws Exception           业务定义的异常。由于thrift生成的异常继承至Exception，所以只能这么抛。
     * @see VoaRuntimeException
     */
    protected final Object doInvokeLogic(Object proxy, Method method, Object[] args) throws VoaRuntimeException, Exception {

        String serviceId = serviceDefinition.getServiceId();
        String version = serviceDefinition.getVersion();
        if (logger.isDebugEnabled()) {
            logger.debug("Invoke service: " + serviceId + ", version: " + version + ", method: " + method);
        }

        List<VoaTransport> xoaTransportList = null;
        try {
            xoaTransportList = serviceRouter.routeCemsService(serviceId, version, timeOut, this.filter);
        } catch (Exception e) {
            throw new VoaTransportException("Failed to route " + serviceId + " version " + version,
                    e);
        }
        if (xoaTransportList == null || xoaTransportList.size() == 0) {
            throw new VoaTransportException("No transport avalible for " + serviceId + " version "
                    + version);
        }
        // Service返回的结果
        StringBuffer resultBuffer = new StringBuffer();
        resultBuffer.append("[");
        Object result = null;
        // Service抛出的异常，将会被抛出
        Throwable serviceException = null;
        // 需要汇报给ServiceRouter的异常，将会被转换成XoaRuntimeException
        Throwable frameworkException = null;
        for (VoaTransport xoaTransport : xoaTransportList) {
            try {

                TProtocol protocol = new TBinaryProtocol(xoaTransport.getTransport());
                Object client = serviceDefinition.getServiceClientConstructor().newInstance(protocol);

                result = getRealMethod(method).getMethod().invoke(client, args);
                String resultStr = byteBuffer2String((ByteBuffer) result);
                Node node = xoaTransport.getNode();
                resultStr = appendReturnInfo(resultStr, node);
                resultBuffer.append(resultStr + ",");
            } catch (InvocationTargetException e) {
                // 反射执行Service抛出的异常
                Throwable cause = e.getCause();
                if (cause instanceof org.apache.thrift.TBase<?, ?>) {
                    // Service抛出异常
                    serviceException = cause;
                    throw (Exception) cause;
                } else {
                    if (cause instanceof org.apache.thrift.TException) {
                        // thrift框架里的异常
                        frameworkException = cause;
                        if (cause instanceof TTransportException) {
                            throw new VoaTransportException(cause);
                        }
                    }
                    throw new VoaRuntimeException(e);
                }
            } catch (Exception e) {
                // 其他异常，捕捉一下漏网之鱼
                frameworkException = e;
                // XOA框架Client抛出异常
                throw new VoaTransportException(e);
            } finally {
                if (frameworkException != null) {
                    // 汇报异常给ServiceRouter
                    if (logger.isDebugEnabled()) {
                        logger.debug("Occur framework exception: " + frameworkException, frameworkException);
                    }
                    serviceRouter.serviceException(serviceId, version, frameworkException, xoaTransport);
                    onFrameworkException(frameworkException);
                } else {
                    // 返还XoaTransport
                    if (logger.isDebugEnabled()) {
                        logger.debug("Return value: " + result + ", exception: " + serviceException,
                                serviceException);
                    }
                    serviceRouter.returnConn(xoaTransport);
                }
            }
        }
        String retResult = resultBuffer.substring(0, resultBuffer.length() - 1) + "]";
        if (logger.isDebugEnabled()) {
            logger.debug("Invoke service return result:" + retResult);
        }
        return ByteBuffer.wrap(retResult.getBytes(ENCODING_DEFAULT));
    }

    /**
     * 当发生框架异常时调用的方法 TODO 这是个比较牵强的做法，只是为了记录框架里的异常，后续
     *
     * @param exception
     */
    protected void onFrameworkException(Throwable exception) {

    }

    /**
     * 获得方法定义，首先会从缓存中取数据，如果缓存中没有则通过反射的方式获得
     *
     * @param method
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    private MethodDefinition getRealMethod(Method method) throws SecurityException,
            NoSuchMethodException {

        MethodDefinition methodDef = methodCache.get(method);

        if (methodDef != null) { // 先从缓存中找方法定义，缓存中有数据直接返回
            return methodDef;
        }

        Method realMethod = serviceDefinition.getServiceClientClass().getMethod(method.getName(),
                method.getParameterTypes());
        methodDef = new MethodDefinition(realMethod);
        methodCache.put(method, methodDef);
        return methodDef;
    }

    /**
     * ByteBuffer转成String
     */
    private String byteBuffer2String(ByteBuffer data) {

        Charset charset = null;
        CharsetDecoder decoder = null;
        CharBuffer charBuffer = null;
        try {
            charset = Charset.forName(ENCODING_DEFAULT);
            decoder = charset.newDecoder();
            charBuffer = decoder.decode(data.asReadOnlyBuffer());
            return charBuffer.toString();
        } catch (Exception e) {
            logger.error("[ByteBuffer]转[String]失败", e);
            return null;
        }
    }

    /**
     * 增加jdata返回失败信息
     */
    private String appendReturnInfo(String result, Node node) {

        StringBuffer sBuffer = new StringBuffer();
        // {"maxCode":"00FF0A00","minCode":"1","result":"0","desc":"xxx","jdata":[]}
        // {"maxCode":"00FF0A00","minCode":"1","result":"0","desc":"xxx","jdata":[{"A":"A"}]}
        int i = result.indexOf("jdata") + 8;
        sBuffer.append(result.substring(0, i));
        if (result.substring(i, i + 1).equals("{")) {
            sBuffer.append("\"serviceIp\":\"" + node.getHost() + "\",\"servicePort\":\"" + node.getPort() + "\",");
            sBuffer.append(result.substring(i + 1, result.length()));
        } else {
            sBuffer.append("{\"serviceIp\":\"" + node.getHost() + "\",\"servicePort\":\"" + node.getPort() + "\"");
            sBuffer.append("}]}");
        }

        return sBuffer.toString();
    }

}
