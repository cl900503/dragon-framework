/**
 * @(#)ServiceInvocationHandler.java, 2012-11-16. 
 * 
 * Copyright 2012 RenRen, Inc. All rights reserved.
 */
package com.vrv.framework.client;

import com.vrv.framework.client.definition.ClassDefinition;
import com.vrv.framework.client.definition.MethodDefinition;
import com.vrv.framework.client.filter.NodeFilter;
import com.vrv.framework.common.exception.VoaRuntimeException;
import com.vrv.framework.common.exception.VoaTransportException;
import com.vrv.framework.common.intercept.VrvClientInterceptHandle;
import com.vrv.framework.common.intercept.VrvClientMethodInvoke;
import com.vrv.framework.common.intercept.VrvClinetInterceptHelp;
import com.vrv.framework.common.spi.Convert2StringProvider;
import com.vrv.framework.common.spi.ProtocolFactoryProvider;
import com.vrv.framework.common.spi.string.Convert2String;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * proxy的InvocationHandler
 * 
 * @author Xun Dai <xun.dai@renren-inc.com>
 */
public class ServiceInvocationHandler implements InvocationHandler {

	static Logger logger = LoggerFactory.getLogger(ServiceInvocationHandler.class);
	private String ip;
	private int port;
	private String protocol;

	private ClassDefinition serviceDefinition;

	private ConcurrentMap<Method, MethodDefinition> methodCache = new ConcurrentHashMap<Method, MethodDefinition>();

	private ServiceRouter serviceRouter;

	private NodeFilter filter;

	private long timeOut;
	
	private Convert2String cs = Convert2StringProvider.getConvert2String();

	public ClassDefinition getServiceDefinition() {

		return serviceDefinition;
	}

	public ConcurrentMap<Method, MethodDefinition> getMethodCache() {

		return methodCache;
	}

	public ServiceRouter getServiceRouter() {

		return serviceRouter;
	}

	public ServiceInvocationHandler(ServiceRouter serviceRouter, ClassDefinition serviceDefinition, long timeOut, NodeFilter filter) {

		if (serviceDefinition == null || serviceRouter == null) {
			throw new NullPointerException();
		}
		this.serviceRouter = serviceRouter;
		this.serviceDefinition = serviceDefinition;
		this.timeOut = timeOut;
		this.filter = filter;

	}

	public ServiceInvocationHandler(ServiceRouter serviceRouter, ClassDefinition serviceDefinition, String ip, int port, String protocol,
			long timeOut) {

		if (serviceDefinition == null || serviceRouter == null) {
			throw new NullPointerException();
		}
		this.serviceRouter = serviceRouter;
		this.serviceDefinition = serviceDefinition;
		this.timeOut = timeOut;
		this.ip = ip;
		this.port = port;
		this.protocol = protocol;
	}

	@Override
	public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return round(proxy, method, args);
	}

	protected Object round(Object proxy, Method method, Object[] args) throws Throwable {

		try {

			beforeInvoke(proxy, method, args);
			return doInvoke(proxy, method, args);
		} catch (Exception e) {

			throw e;
		} finally {
			afterInvoke(proxy, method, args);
		}
	}

	protected void beforeInvoke(Object proxy, Method method, Object[] args) throws Throwable {

	}

	protected void afterInvoke(Object proxy, Method method, Object[] args) {

	}

	protected final Object doInvoke(Object proxy, Method method, Object[] args) throws VoaRuntimeException, Exception {

		// Service返回的结果
		Object result = null;
		try {
			result = doInvokeLogic(proxy, method, args);
		} catch (Exception e) {
			logger.error("==========>>>>doInvokeLogic Exception<<<<==========");
			// 如果是网络异常，并重连一次。
			Throwable cause = e.getCause();
			if (!(cause instanceof TTransportException)) {
				logger.error("==========>>>>throw Exception<<<<==========", e);
				throw e;
			}
			if (e instanceof InvocationTargetException) {
				InvocationTargetException invocationExp = (InvocationTargetException) e;
				if (invocationExp.getTargetException().getCause() instanceof SocketTimeoutException) {
					logger.error("==========>>>>throw InvocationTargetException<<<<==========", e);
					throw e;
				}
			}

			// 其他的异常，再回调重试一次
			logger.error("网络处理异常,开始重连一次...", e);
			result = doInvokeLogic(proxy, method, args);
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

		if (StringUtils.isNotBlank(ip) && port != 0) {
			return doInvokeLogicWithIpPort(proxy, method, args);
		}
		String serviceId = serviceDefinition.getServiceId();
		String version = serviceDefinition.getVersion();

		/*
		 * 远程调用Service，返回结果，返还Transport，处理异常 XOASEC-148
		 * 如果无可用transport累计一定次数，发报警短信，通知的是client developer这个功能去哪了呢？
		 * 我决定把这个功能添加到CommonServiceRouter里面。没有这个显然是不行的。
		 */
		VoaTransport xoaTransport = null;
		try {

			long begin = System.currentTimeMillis();
			xoaTransport = serviceRouter.routeService(serviceId, version, null, timeOut, this.filter);
			long end = System.currentTimeMillis();
			if (logger.isDebugEnabled()) {
				logger.debug("Invoke service: serviceRouter.routeService cost==>{}ms", end - begin);
			}
		} catch (Exception e) {
			throw new VoaTransportException("Failed to route " + serviceId + " version " + version, e);
		}
		if (xoaTransport == null) {
			throw new VoaTransportException("No transport avalible for " + serviceId + " version " + version);
		}
		if (xoaTransport.getNode() == null) {
			throw new VoaTransportException("xoaTransport.node is null");
		}
		
		// Service返回的结果
		Object result = null;
		// Service抛出的异常，将会被抛出
		Throwable serviceException = null;
		// 需要汇报给ServiceRouter的异常，将会被转换成XoaRuntimeException
		Throwable frameworkException = null;
		try {

//			TProtocol protocol = new TBinaryProtocol(xoaTransport.getTransport());
			
			TProtocol protocol = ProtocolFactoryProvider.getProtocolFactory(xoaTransport.getNode().getProtocol()).clientProtocol(xoaTransport.getTransport());
			
			Object client = serviceDefinition.getServiceClientConstructor().newInstance(protocol);

			Method realMethod = getRealMethod(method).getMethod();
			// 调用拦截器
			VrvClientMethodInvoke target = realMethod::invoke;
			for (VrvClientInterceptHandle v : VrvClinetInterceptHelp.getInterceptHandles()) {
				VrvClientMethodInvoke finalTarget = target;
				target = (service1, args1) -> {
					try {
						return v.intercept(service1, serviceId, args1, realMethod, finalTarget);
					} catch (Exception e) {
						throw e;
					}
				};
			}
			
			logger.info("Invoke service:{},method {}==>start", serviceId, method);

			if (cs.filterMethod(method)) {
				if (args != null) {
					int length = args.length;
					for (int i = 0; i < length; i++) {
						logger.debug("Invoke service: method {} arg {}==>{}", method.getName(), i,
								cs.convert2String(args[i]));
					}
				}
			}

			long invokeBegin = System.currentTimeMillis();
			result = target.invoke(client, args);
			long invokeEnd = System.currentTimeMillis();
			logger.info("Invoke service: cost==>{}ms", invokeEnd - invokeBegin);

			if (cs.filterMethod(method)) {
				logger.debug("Invoke service methodName==>{}===========result ==>{}", method.getName(),
						cs.convert2String(result));
			}

			return result;
		} catch (Exception e) {
			// 其他异常，捕捉一下漏网之鱼
			frameworkException = e;
			// XOA框架Client抛出异常
			// throw new VoaTransportException(e);
			throw e;
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
					logger.debug("Invoke service: Return value:{},exception:{}", result, serviceException);
				}
				serviceRouter.returnConn(xoaTransport);
			}
		}
	}

	protected final Object doInvokeLogicWithIpPort(Object proxy, Method method, Object[] args) throws VoaRuntimeException, Exception {
		String serviceId = serviceDefinition.getServiceId();
		String version = serviceDefinition.getVersion();

		/*
		 * 远程调用Service，返回结果，返还Transport，处理异常 XOASEC-148
		 * 如果无可用transport累计一定次数，发报警短信，通知的是client developer这个功能去哪了呢？
		 * 我决定把这个功能添加到CommonServiceRouter里面。没有这个显然是不行的。
		 */
		VoaTransport xoaTransport = null;
		try {

			long begin = System.currentTimeMillis();
			xoaTransport = serviceRouter.routeService(serviceId, version, this.ip, this.port, this.protocol, timeOut);
			long end = System.currentTimeMillis();
			if (logger.isDebugEnabled()) {
				logger.debug("Invoke service: serviceRouter.routeService cost==>{}ms", end - begin);
			}
		} catch (Exception e) {
			throw new VoaTransportException("Failed to route " + serviceId + " version " + version, e);
		}
		if (xoaTransport == null) {
			throw new VoaTransportException("No transport avalible for " + serviceId + " version " + version);
		}
		if (xoaTransport.getNode() == null) {
			throw new VoaTransportException("xoaTransport.node is null");
		}
		// Service返回的结果
		Object result = null;
		// Service抛出的异常，将会被抛出
		Throwable serviceException = null;
		// 需要汇报给ServiceRouter的异常，将会被转换成XoaRuntimeException
		Throwable frameworkException = null;
		try {
//			TProtocol protocol = new TBinaryProtocol(xoaTransport.getTransport());
			
			TProtocol protocol = ProtocolFactoryProvider.getProtocolFactory(xoaTransport.getNode().getProtocol()).clientProtocol(xoaTransport.getTransport());
			
			Object client = serviceDefinition.getServiceClientConstructor().newInstance(protocol);

			Method realMethod = getRealMethod(method).getMethod();
			// 调用拦截器
			VrvClientMethodInvoke target = realMethod::invoke;
			for (VrvClientInterceptHandle v : VrvClinetInterceptHelp.getInterceptHandles()) {
				VrvClientMethodInvoke finalTarget = target;
				target = (service1, args1) -> {
					try {
						return v.intercept(service1, serviceId, args1, realMethod, finalTarget);
					} catch (Exception e) {
						throw e;
					}
				};
			}
			result = target.invoke(client, args);

			return result;
		} catch (InvocationTargetException e) {
			// 反射执行Service抛出的异常
			Throwable cause = e.getCause();
			if (cause instanceof org.apache.thrift.TBase<?, ?>) {
				// Service抛出异常
				serviceException = cause;
				throw (Exception) cause;
			}
			if (cause instanceof org.apache.thrift.TException) {
				// thrift框架里的异常
				frameworkException = cause;
				if (cause instanceof TTransportException) {
					throw new VoaTransportException(cause);
				} /*
					 * else if (cause instanceof TApplicationException) {
					 *
					 * } else if (cause instanceof TProtocolException) {
					 *
					 * }
					 */
			}
			throw new VoaRuntimeException(e);
		} catch (Exception e) {
			// 其他异常，捕捉一下漏网之鱼
			frameworkException = e;
			// XOA框架Client抛出异常
			throw new VoaTransportException(e);
		} finally {
			if (frameworkException != null) {
				// 汇报异常给ServiceRouter
				logger.error("doInvokeLogicWithIpPort Occur framework exception: " + frameworkException, frameworkException);
				serviceRouter.serviceApException(serviceId, version, frameworkException, xoaTransport, ip, port);
				onFrameworkException(frameworkException);
			} else {
				// 返还XoaTransport
				if (logger.isDebugEnabled()) {
					logger.debug("Invoke service: Return value:{},exception:{}", result, serviceException);
				}
				serviceRouter.returnConn(xoaTransport);
			}
		}
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
	private MethodDefinition getRealMethod(Method method) throws SecurityException, NoSuchMethodException {

		MethodDefinition methodDef = methodCache.get(method);

		if (methodDef != null) { // 先从缓存中找方法定义，缓存中有数据直接返回
			return methodDef;
		}

		Method realMethod = serviceDefinition.getServiceClientClass().getMethod(method.getName(), method.getParameterTypes());
		methodDef = new MethodDefinition(realMethod);
		methodCache.put(method, methodDef);
		return methodDef;
	}

}
