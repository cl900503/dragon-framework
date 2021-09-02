include "vrv.thrift"
include "aphead.thrift"
namespace java com.vrv.example.base.thrift

service UserService extends vrv.VrvService {

    /**
     * 获取用户信息
     */
    aphead.User getUser(1: i64 userId)

}