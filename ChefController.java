package nam.nguyen.store.controller;

import nam.nguyen.store.model.Invoice;
import nam.nguyen.store.model.UpdateStatusInvoice;
import nam.nguyen.store.service.HelpService;
import nam.nguyen.store.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/chef")
public class ChefController {
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private HelpService helpService;

    @GetMapping("/home")
    public String viewHomeCheff(Model model) {
        List<Invoice> invoices = invoiceService.findInvoicesChef();
        model.addAttribute("invoices", invoices);
        return "chef-home";
    }
    @GetMapping("/detailtable/{id}")
    public String detailTableCheff(Model model , @PathVariable("id")Integer id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        model.addAttribute("invoice", invoice);
        return "chef-detail-table";
    }
    @GetMapping("/confirmOrder/{id}")
    public String confirmOrder(@PathVariable("id") Integer id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        if (invoice.getStatusService() < 3 && invoice.getStatusService() >= 0) {
            invoice.setStatusService(invoice.getStatusService() + 1);
            invoiceService.saveInvoice(invoice);
        }
        UpdateStatusInvoice updateStatusInvoice = new UpdateStatusInvoice();
        updateStatusInvoice.setId(invoice.getId());
        if (invoice.getStatusService()==2){
            String temp="bàn số : "+ invoice.getIdTable() +" món ăn đã chế biến xong ";
            helpService.addMessage(temp);
            messagingTemplate.convertAndSend("/topic/help",temp );

        }

        return "redirect:/chef/detailtable/" + invoice.getId();    }
    @GetMapping("/RefuseOrder/{id}")
    public String RefuseOrder(@PathVariable("id") Integer id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        if (invoice.getStatusService() >= 0) {
            invoice.setStatusService(4);
            invoiceService.saveInvoice(invoice);
        }

        return "redirect:/chef/detailtable/" + invoice.getId();
    }
}
