package com.MySpringBoot.my_first_app.controler;

import com.MySpringBoot.my_first_app.service.AlipayService;
import com.MySpringBoot.my_first_app.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pay")
public class PayController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private JwtUtil jwtUtil;

    private Integer getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        return null;
    }

    /**
     * 生成支付宝支付表单
     */
    @PostMapping("/alipay")
    public Map<String, Object> alipay(@RequestBody Map<String, Integer> params, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }

        Integer orderId = params.get("orderId");
        if (orderId == null) {
            response.put("code", 400);
            response.put("message", "缺少订单ID");
            return response;
        }

        String form = alipayService.createPayForm(orderId);
        if (form == null) {
            response.put("code", 500);
            response.put("message", "生成支付表单失败");
            return response;
        }

        response.put("code", 200);
        response.put("message", "success");
        response.put("data", form);
        return response;
    }

    /**
     * 支付宝异步通知回调（POST）
     */
    @PostMapping("/alipay/notify")
    public String notify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            params.put(entry.getKey(), String.join(",", entry.getValue()));
        }

        // 验证签名
        boolean verified = alipayService.verifyNotify(params);
        if (!verified) return "failure";

        // 处理业务
        String tradeStatus = params.get("trade_status");
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            String orderNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            alipayService.handlePaySuccess(orderNo, tradeNo);
        }

        return "success";
    }

    /**
     * 支付宝同步回调（GET）— 重定向到前端订单页
     */
    @GetMapping("/alipay/return")
    public void returnUrl(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String orderNo = request.getParameter("out_trade_no");
        response.sendRedirect("/orders?paid=" + (orderNo != null ? orderNo : ""));
    }
}
