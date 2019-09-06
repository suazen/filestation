package me.daylight.filestation.authority;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.daylight.filestation.util.FileUtil;
import me.daylight.filestation.util.RetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @author Daylight
 * @date 2019/1/8 16:40
 */
public class AuthorityInterceptor implements HandlerInterceptor {
    @Autowired
    private ObjectMapper objectMapper;

    private static Logger logger= LoggerFactory.getLogger(AuthorityInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerMethod handlerMethod = null;
        if (handler instanceof HandlerMethod)
            handlerMethod = (HandlerMethod) handler;

        logger.info("-------------------------------------------------------------------------------------");
        logger.info("-->URL:" + request.getRequestURI());
        logger.info("-->HTTP_METHOD:" + request.getMethod());
        logger.info("-->IP:" + request.getRemoteAddr());
        logger.info("-->SERVER_PORT:" + request.getServerPort());
        logger.info("-->USER:" + (SessionUtil.getAttribute("user")));
        logger.info("-->SESSION:" + request.getSession().getId());
        logger.info("-------------------------------------------------------------------------------------");

        if (!Objects.isNull(handlerMethod)) {
            Limit annotation;
            if (handlerMethod.hasMethodAnnotation(Limit.class))
                annotation = handlerMethod.getMethodAnnotation(Limit.class);
            else
                annotation = handlerMethod.getMethod().getDeclaringClass().getDeclaredAnnotation(Limit.class);

            if (!Objects.isNull(annotation)&&annotation.loginRequired()) {
                if (SessionUtil.getAttribute("user") == null) {
                    if (annotation.returnType().equals(ReturnType.Page))
                        response.sendRedirect("/");
                    else
                        response.getWriter().write(objectMapper.writeValueAsString(RetResponse.error("please login first")));
                    return false;
                }
                SessionUtil.setExpire();
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        HandlerMethod handlerMethod = null;
        if (handler instanceof HandlerMethod)
            handlerMethod = (HandlerMethod) handler;

        if (!Objects.isNull(handlerMethod)) {
            if (handlerMethod.getMethod().getName().equals("download")){
                FileUtil.delFolder("preview/");
            }
        }
    }
}
