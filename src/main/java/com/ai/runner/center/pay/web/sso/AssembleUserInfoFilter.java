//package com.ai.runner.center.pay.web.sso;
//
//import java.io.IOException;
//import java.lang.reflect.Field;
//import java.security.Principal;
//import java.util.Map;
//
//import javax.servlet.Filter;
//import javax.servlet.FilterChain;
//import javax.servlet.FilterConfig;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpSession;
//
//import org.apache.log4j.Logger;
//import org.jasig.cas.client.authentication.AttributePrincipal;
//
//import com.ai.runner.framepage.util.OperInfoUtil;
//import com.ai.runner.utils.web.model.UserLoginVo;
//
//
//public class AssembleUserInfoFilter implements Filter {
//    private static final Logger LOG = Logger.getLogger(AssembleUserInfoFilter.class);
//
//    public void init(FilterConfig filterConfig) throws ServletException {
//      
//    }
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response,
//                         FilterChain chain) throws IOException, ServletException {
//        HttpServletRequest req = (HttpServletRequest) request;
//        HttpSession session = req.getSession();
//        UserLoginVo user = (UserLoginVo) session.getAttribute(OperInfoUtil.USER_SESSION_KEY);
//        LOG.info("已封装的用户信息为：" + user);
//        if (user == null) {
//            user = assembleUser(req);
//
//			/*非营业频道登录 或者 相关属性 为空进行控制*/
//            LOG.info("已封装的用户信息为：" + user.toString());
//            if (user == null || user.getOperCode() == null || "".equals(user.getOperCode()) ||
//                    user.getTenantId() == null || "".equals(user.getTenantId())) {
//            } else {
//                session.setAttribute(OperInfoUtil.USER_SESSION_KEY, user);
//                chain.doFilter(req, response);
//            }
//        } else {
//            chain.doFilter(req, response);
//        }
//    }
//
//    @Override
//    public void destroy() {
//
//    }
//
//    /**
//     * 封装用户信息
//     *
//     * @param request
//     * @return
//     */
//    private UserLoginVo assembleUser(HttpServletRequest request) {
//        UserLoginVo user = null;
//        try {
//            Principal principal = request.getUserPrincipal();
//            if (principal != null) {
//                user = new UserLoginVo();
//                AttributePrincipal attributePrincipal = (AttributePrincipal) principal;
//                Map<String, Object> attributes = attributePrincipal.getAttributes();
//                Field[] fields = UserLoginVo.class.getDeclaredFields();
//                for (Field field : fields) {
//                    String value = (String) attributes.get(field.getName());
//                    if (value != null) {
//                        field.setAccessible(true);
//                        if ("long".equals(field.getType().toString())) {
//                            field.set(user, Long.parseLong(value));
//                        } else {
//                            field.set(user, value);
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            LOG.error("封装用户信息失败", e);
//        }
//        return user;
//    }
//}
