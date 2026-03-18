package org.nembx.app.common.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Lian
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    SUCCESS("成功", 200),
    BAD_REQUEST("请求参数错误", 400),
    UNAUTHORIZED("未授权", 401),
    FORBIDDEN("禁止访问", 403),
    NOT_FOUND("资源不存在", 404),
    PARAM_ERROR("参数错误", 422),
    INTERNAL_ERROR("服务器内部错误", 500),
    USER_NOT_FOUND("用户不存在", 10001),
    USER_EXIST("用户已存在", 10002),
    USER_NOT_LOGIN("用户未登录", 10003),
    USER_NOT_ADMIN("用户不是管理员", 10004),
    USER_NOT_PERMISSION("用户无权限", 10005),
    USER_NOT_EXIST("用户不存在", 10006),
    USER_NOT_ACTIVE("用户未激活", 10007),
    USER_NOT_LOGOUT("用户未登出", 10008),
    DELETE_FAIL("删除失败", 10009),
    UPLOAD_FAIL("上传失败", 10010),
    DOWNLOAD_FAIL("下载失败", 10011),
    KNOWLEDGE_QUERY_ERROR("知识库查询错误", 10012),
    EXPORT_PDF_FAILED("导出PDF失败", 10013),
    VECTOR_ERROR("向量库错误", 10014),
    AI_CALL_ERROR("AI调用失败", 10015);

    private final String message;
    private final Integer code;
}
