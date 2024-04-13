package nam.nguyen.store.controller;

import nam.nguyen.store.model.*;
import nam.nguyen.store.service.FeedBackService;
import nam.nguyen.store.service.InvoiceService;
import nam.nguyen.store.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
public class ChatGPTController {

    @Value("${openai.api.key}") // Đặt giá trị trong file application.properties hoặc application.yml
    private String openaiApiKey;
    @Autowired
    private ProductService productService;
     @Autowired
      private InvoiceService invoiceService;
    @Autowired
    private FeedBackService feedBackService;
    String request = "bạn sẽ là nhân viên tư vấn khách hàng cho cửa hàng của tôi công việc của bạn sẽ là " +
            "trả lời câu hỏi của khách hàng một cách ngắn gọn khi tư vấn cho khách hãy nhớ xuống dòng để khách dễ đọc sau khi tôi cung cấp cho bạn thông tin về cửa hàng của tôi dựa vào thông" +
            " tin tôi sẽ cung cấp bạn sẽ trả lời câu hỏi của khách hàng";
    String info= "đây là thông tin nhà của tôi  hàng Tên Nhà Hàng: Nam Nguyễn Food Địa Chỉ: 65 phạm hữu lầu, Thành phố dĩ an," +
            " tỉnh bình dương Số Điện Thoại: 0389510507  Giới Thiệu:Nam Nguyễn Food là một nhà hàng hiện " +
            "đại chuyên phục vụ ẩm thực đa dạng từ nhiều quốc gia. Với không gian ấm cúng và trang trí hiện đại," +
            " nhà hàng tập trung vào việc cung cấp trải nghiệm ẩm thực đẳng cấp, từ các món ăn sáng sang trọng đến các" +
            " món tráng miệng tinh tế. Menu đa dạng của chúng tôi chủ yếu được làm từ nguyên liệu tươi sạch và chất" +
            " lượng cao.Giờ Mở Cửa:Thứ Hai đến Thứ Sáu: 10:00 sáng - 10:00 tốiThứ Bảy và Chủ Nhật: 12:00 trưa - 11:00 tốiĐặt" +
            " Bàn:Để đặt bàn, vui lòng gọi điện thoại hoặc sử dụng dịch vụ đặt bàn trực tuyến trên trang web của chúng tôi , thực đơn" +
            "gồm các món :";
    String menu ="";
    String statustable="";
    @Scheduled(fixedRate = 60000) // 1 minutes = 15 * 60 * 1000 milliseconds
    public void updateInfoStoreForAI() {
        List<Product> products = productService.getAllProducts();
        menu="";
        statustable="thông tin tất cả các hóa đơn chưa thanh toán ";
        Locale localeVN = new Locale("vi", "VN");
        NumberFormat numberFormat = NumberFormat.getNumberInstance(localeVN);

        for (Product p: products) {
            menu+="tên:"+p.getName()+" giá: "+numberFormat.format(p.getPrice())+" mô tả: "+p.getDescription()+", ";
        }
        List<Invoice> invoices = invoiceService.getInvoiceByStatus(0);
        for (Invoice p: invoices) {
            statustable+="mã hóa đơn:"+p.getId()+"bàn số "+p.getIdTable()+" tổng tiền : "+numberFormat.format(p.getTotal())+"ngàn"+", ";
        }
    System.out.println("Data Ai  đã được cập nhập");
    }


    @GetMapping("/chat")
    public ResponseEntity<String> getChatResult(Model model , @RequestParam("content") String content) {
        String apiUrl = "https://api.openai.com/v1/chat/completions";
        String gptModel = "gpt-3.5-turbo";
        String payload = "{ \"model\": \"" + gptModel + "\", " +
                "\"messages\": [ {\"role\": \"user\", \"content\": \""+request+"\"}," +
                "{\"role\": \"user\", \"content\": \""+info+menu+statustable+"\"}," +
                "{\"role\": \"user\", \"content\": \""+content+"\"}] }";
        RestTemplate restTemplate = new RestTemplate();
        OpenAiResponse response = restTemplate.postForObject(apiUrl, createHttpEntity(payload), OpenAiResponse.class);
        return new ResponseEntity<>(response.getChoices().get(0).getMessage().getContent(), HttpStatus.OK);
    }
    @GetMapping("/khuyennghi")
    public String getKhuyenNghi(Model model ) {
        String apiUrl = "https://api.openai.com/v1/chat/completions";
        String gptModel = "gpt-3.5-turbo";
        String payload = "{ \"model\": \"" + gptModel + "\", " +
                "\"messages\": [ {\"role\": \"user\", \"content\": \""+getDataChatGPT()+"\"}," +
                "{\"role\": \"user\", \"content\": \"\"}," +
                "{\"role\": \"user\", \"content\": \"\"}] }";
        RestTemplate restTemplate = new RestTemplate();
        OpenAiResponse response = restTemplate.postForObject(apiUrl, createHttpEntity(payload), OpenAiResponse.class);
        model.addAttribute("content",response.getChoices().get(0).getMessage().getContent());
        System.out.println(response.getChoices().get(0).getMessage().getContent());
        return "khuyennghi";
    }
    private String getDataChatGPT() {
        List<FeedBack> temps = feedBackService.getNewFeedBack();
        StringBuilder contents = new StringBuilder("Đây là 30 đánh giá gần nhất của cửa hàng được phân cách nhau bởi dấu ';'." +
                " Hãy đưa ra các khuyến nghị cho chủ cửa hàng để kinh doanh tốt hơn trả lời 1 cách ngắn gọn. ");

        String[] saoContents = new String[5];

        for (FeedBack feedback : temps) {
            int stars = feedback.getStars();
            if (stars >= 1 && stars <= 5) {
                if (saoContents[stars - 1] == null) {
                    saoContents[stars - 1] = feedback.getContent() + "," + feedback.getStars() + " sao,sdt "+feedback.getPhone()+";";
                } else {
                    saoContents[stars - 1] += feedback.getContent() + "," + feedback.getStars() + " sao ,sdt "+feedback.getPhone()+";";
                }
            }
        }
        for (String saoContent : saoContents) {
            if (saoContent != null) {
                contents.append(saoContent);
            }
        }
        System.out.println(contents.toString());
        return contents.toString();
    }

    private HttpEntity<String> createHttpEntity(String payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openaiApiKey);
        return new HttpEntity<>(payload, headers);
    }


}
