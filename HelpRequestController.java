package nam.nguyen.store.controller;

import nam.nguyen.store.service.HelpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/help")
public class HelpRequestController {
    @Autowired
    private  SimpMessagingTemplate messagingTemplate;
    @Autowired
    private HelpService helpService;

    @PostMapping("/delete-help")
    @ResponseBody
    public String deleteHelp(@RequestBody Map<String, String> requestData) {
        String messenger = requestData.get("messenger");

        // Thực hiện xóa tin nhắn từ danh sách tạm thời
        helpService.removeMessage(messenger);

        // Trả về phản hồi cho máy khách
        return "Message deleted successfully"; // Hoặc bất kỳ phản hồi nào bạn muốn trả về
    }
    @GetMapping("/gethelp")
    public ResponseEntity<List<String>> getAllHelp() {

        return ResponseEntity.ok(helpService.getAllMessages());
    }


    @Autowired
    public HelpRequestController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }



    @PostMapping("/request-help")
    public void handleHelpRequest(@RequestParam("tableNumber") String tableNumber,@RequestParam("message") String message ) {
        String temp = "Bàn Số :  "+ tableNumber + " Yêu Cầu:  "+message;
        helpService.addMessage(temp);
        messagingTemplate.convertAndSend("/topic/help", temp);
    }
}
