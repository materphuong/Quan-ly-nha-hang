package nam.nguyen.store.controller;

import nam.nguyen.store.model.Invoice;
import nam.nguyen.store.service.HelpService;
import nam.nguyen.store.service.InvoiceService;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Controller
public class PaymentController {
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private HelpService helpService;
    @PostMapping("/paymentMethod")
    public String paymentMethod(@RequestParam("paymentMethod") String paymentMethod ,
                                @RequestParam("total") String total,
                                @RequestParam("id") String id,
                                @RequestParam("idtable") String idtable){
        if (paymentMethod.equals("momo")){
            LocalDateTime ngayGio = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String ngayGioFormatted = ngayGio.format(formatter);
            String oderId="";
            if(id.isEmpty()){
                oderId= "ban"+idtable+"H"+ngayGioFormatted;
            }else {
               oderId= "hoadon"+id+"H"+ngayGioFormatted;
            }
            return createPayment(total,oderId);
        }else {
            String temp="bàn số : "+ idtable +" Yêu cầu thanh toán tại bàn";
            helpService.addMessage(temp);
            messagingTemplate.convertAndSend("/topic/help",temp );
        }
        return "redirect:/feedback/feedback/"+idtable;
    }
    public String createPayment(String total,String id) {
        String partnerCode = "MOMO";
        String accessKey = "F8BBA842ECF85";
        String secretKey = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
        String requestId = partnerCode + new Date().getTime();
        String orderId = id;
        String orderInfo = "pay with MoMo";
        String redirectUrl = "http://localhost:8080/callback";
        String ipnUrl = "http://localhost:8080/customer/succsespay";
        String amount = total;
        String requestType = "captureWallet";
        String extraData = "";
        String rawSignature = "accessKey=" + accessKey + "&amount=" + amount + "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl + "&orderId=" + orderId + "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode + "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId + "&requestType=" + requestType;
        String signature = HmacUtils.hmacSha256Hex(secretKey, rawSignature);
        String requestBody = String.format(
                "{\"partnerCode\":\"%s\",\"accessKey\":\"%s\",\"requestId\":\"%s\",\"amount\":\"%s\",\"orderId\":\"%s\"," +
                        "\"orderInfo\":\"%s\",\"redirectUrl\":\"%s\",\"ipnUrl\":\"%s\",\"extraData\":\"%s\",\"requestType\":\"%s\",\"signature\":\"%s\",\"lang\":\"en\"}",
                partnerCode, accessKey, requestId, amount, orderId, orderInfo, redirectUrl, ipnUrl, extraData, requestType, signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        String endpointUrl = "https://test-payment.momo.vn/v2/gateway/api/create";
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(endpointUrl, httpEntity, String.class);
        String responseBody = responseEntity.getBody();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            String payUrl = jsonNode.get("payUrl").asText();

            System.out.println("PayUrl: " + payUrl);
            return "redirect:"+payUrl;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return responseBody;
    }
    @GetMapping("/callback")
    public String handleMoMoCallback(
            @RequestParam("orderId") String orderId,
            @RequestParam("transId") String transId,
            @RequestParam("resultCode") String resultCode,
            @RequestParam("message") String message,
            @RequestParam("signature") String signature) {
        Invoice invoice = new Invoice();
            if ("0".equals(resultCode)) {
                int viTriH = orderId.indexOf('H');

                // cập nhập đơn hàng nếu khách thanh toán theo hóa đơn
                if (orderId.contains("hoadon")){
                    String maHD = orderId.substring(6, viTriH);
                    invoice = invoiceService.getInvoiceById(Integer.parseInt(maHD));
                    invoice.setStatusPay(1);
                    invoiceService.saveInvoice(invoice);
                    return "redirect:/customer/viewtable/"+invoice.getIdTable();
                }else {
                    // thanh toán theo bàn
                    String maHD = orderId.substring(3, viTriH);
                    List<Invoice> invoices = invoiceService.findInvoiceByIdTable(Integer.parseInt(maHD));
                    for (Invoice i:invoices) {
                        i.setStatusPay(1);
                    }
                    invoiceService.savesInvoice(invoices);
                    return "redirect:/customer/viewtable/"+maHD;
                }

            } else {
                // Xử lý trường hợp thanh toán không thành công
                return "redirect:/customer/viewcart/"+invoice.getIdTable();
            }
        }
}
