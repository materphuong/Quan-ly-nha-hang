package nam.nguyen.store.controller;

import nam.nguyen.store.model.*;
import nam.nguyen.store.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Controller
@RequestMapping("/staff")
public class StaffController {
    @Autowired
    DiningTableService tableService;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private UserService userService;
    @Autowired
    private DiningTableService diningTableService;
    @Autowired
    private InvoiceProductService invoiceProductService;



    @GetMapping("/viewalltable")
    public String viewTable(Model model) {
        model.addAttribute("table", diningTableService.viewTable());
        return "staff-view-all-table";
    }
    @GetMapping("/viewtablebyid/{id}")
    public String setTable(@PathVariable("id") Integer id , Model model) {
        DiningTable table = tableService.getTable(id);
        List<DiningTable> tables = new ArrayList<>();
        tables.add(table);
        model.addAttribute("table", tables);
        return "staff-view-all-table";
    }
    @GetMapping("/viewtable/{id}")
    public String viewlTable(Model model, @PathVariable("id") Integer id) {
        List<Invoice> invoice = invoiceService.findInvoiceByIdTable(id);
        model.addAttribute("invoices", invoice);
        long temp = invoice.stream().mapToLong(Invoice::getTotal).sum();
        model.addAttribute("sumtotal", temp);
        model.addAttribute("idtable", id);
        return "staff-view-table";
    }
    @GetMapping("/detailtable/{id}")
    public String detailTable(Model model , @PathVariable("id")Integer id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        model.addAttribute("invoice", invoice);
        return "staff-detail-table";
    }
    @GetMapping("/help")
    public String help() {
        return "staff-view-help";
    }

    @GetMapping("/home")
    public String viewHome(Model model) {
        List<Invoice> invoices = invoiceService.findUnpaidInvoices();
        model.addAttribute("invoices", invoices);
        return "staff_home";
    }
    @GetMapping("/findinvoicebystatus/{status}")
    public String findInvoiceByStatus(Model model,@PathVariable("status")Integer status) {
        List<Invoice> invoices = invoiceService.getInvoiceByStatus(status);
        model.addAttribute("invoices", invoices);
        return "staff_home";
    }
    @GetMapping("/findinvoice")
    public String findInvoice(Model model, @RequestParam("id") Integer id) {
        try {
            Invoice invoice = invoiceService.getInvoiceById(id);
            List<Invoice> invoices = new ArrayList<>();
            if (invoice != null) {

                invoices.add(invoice);
                model.addAttribute("invoices", invoices);
            } else {
                model.addAttribute("invoices", invoices);
            }
        } catch (RuntimeException e) {

            model.addAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "staff_home";
    }


    @GetMapping("/invoicetoday")
    public String invoiceToDay(Model model) {
        List<Invoice> invoices = invoiceService.getInvoicesForToday();
        model.addAttribute("invoices", invoices);
        return "staff_home";
    }


    @GetMapping("/viewmenu")
    public  String viewMenu(Model model){
        model.addAttribute("menu",productService.getAllProducts());
        model.addAttribute("categorys",categoryService.getAllCategorys());
        return "staff-menu";
    }

    @GetMapping("/viewchart")
    public String ChartData(Model model) {
        List<Invoice> invoices = invoiceService.getInvoicesForTodaySuccess();
        long total= invoices.stream().mapToLong(Invoice::getTotal).sum();
        model.addAttribute("total",total);
        model.addAttribute("count",invoices.stream().count());
        return "staff-month-report";
    }
    @GetMapping("/chartdata")
    public ResponseEntity<List<Object[]>> getChartData() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        List<Object[]> dataCharts = invoiceService.getDataChartByYear(year);
        return ResponseEntity.ok(dataCharts);
    }
    @GetMapping("/viewchartproduct")
    public String ChartProductData(Model model) {
        return "staff-food-sales";
    }
    @GetMapping("/productreport")
    public @ResponseBody List<Object[]> productCountReport(){
        return invoiceProductService.countProduct();
    }
    @PostMapping("/confirmpayment")
    public String confirmpayment(@RequestParam("id") Integer id){
        Invoice invoice = invoiceService.getInvoiceById(id);
        invoice.setStatusPay(1);
        invoiceService.saveInvoice(invoice);
        return "redirect:/staff/detailtable/"+invoice.getId();
    }
    @PostMapping("/confirmpaymenttable")
    public String confirmpaymenttable(@RequestParam("idtable") Integer idtable){
        List<Invoice> invoices = invoiceService.findInvoiceByIdTable(idtable);
        for (Invoice i:invoices) {
            i.setStatusPay(1);
        }
       invoiceService.savesInvoice(invoices);
        return "redirect:/staff/viewtable/"+idtable;
    }

}
