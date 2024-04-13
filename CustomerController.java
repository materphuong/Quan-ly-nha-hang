package nam.nguyen.store.controller;

import jakarta.servlet.http.HttpSession;
import nam.nguyen.store.model.*;
import nam.nguyen.store.repository.CategoryRepository;
import nam.nguyen.store.service.*;
import org.apache.catalina.session.StandardSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CartService cartService;
    @Autowired
    private CartItemService cartItemService;
    @Autowired
    private DiningTableService diningTableService;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private InvoiceProductService invoiceProductService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private FeedBackService feedBackService;



    @GetMapping("/home")
    public String viewTable(Model model) {
        model.addAttribute("table", diningTableService.viewTable());
        return "customer-home";
    }

    @GetMapping("/viewmenu/{id}")
    public String viewMenu(Model model,@PathVariable("id") Integer id) {
        List<Category> categories= categoryRepository.findAll();
        model.addAttribute("categorys",categories);
        model.addAttribute("idtable", id);
        model.addAttribute("menu", productService.getAllProductForCustomer(1));
        return "customer-menu";
    }
    @GetMapping("/viewmenucategory")
    public String viewMenuCategory(Model model,@RequestParam("id") Integer id , @RequestParam("idcatefory") Integer idcatefory) {
        List<Category> categories= categoryRepository.findAll();
        model.addAttribute("categorys",categories);
        model.addAttribute("idtable", id);
        List<Product> products=new ArrayList<>();
        for (Category c: categories) {
            if (c.getId()==idcatefory){
                 products.addAll( c.getProducts()) ;
            }
        }
        model.addAttribute("menu",products);
        return "customer-menu";
    }
    @GetMapping("/viewcart/{id}")
    public String viewCart(Model model, @PathVariable("id") Integer id) {
        if (id <= diningTableService.viewTable().size()) {
            model.addAttribute("all_items_in_shoppingcart", cartService.getAllCartItem(id));
            model.addAttribute("total_amount", cartService.total(id));
            model.addAttribute("idtable", id);

        }

        return "customer-cart";
    }
    @GetMapping("/viewother/{id}")
    public String viewOther(Model model, @PathVariable("id") Integer id) {
        model.addAttribute("idtable", id);
        return "customer-other";
    }

    @PostMapping("/addcart")
    public String addCart(@RequestParam("idtable") Integer idtable, @RequestParam("idproduct") Integer idproduct) {
        Product product = productService.getProductById(idproduct);
        cartService.addProduct(product, idtable);
        return "redirect:/customer/viewmenu/" + idtable;
    }

    @PostMapping("/removecart")
    public String removeCart(@RequestParam("idtable") Integer idtable, @RequestParam("idproduct") Integer idproduct) {
        Product product = productService.getProductById(idproduct);
        cartService.removeProduct(product, idtable);
        return "redirect:/customer/viewcart/" + idtable;
    }

    @PostMapping("/updatecart")
    public String updateCart(@RequestParam("idtable") Integer idtable, @RequestParam("idproduct") Integer idproduct,
                             @RequestParam("quantity") Integer quantity) {
        Product product = productService.getProductById(idproduct);
        cartService.updateProduct(product, idtable, quantity);
        return "redirect:/customer/viewcart/" + idtable;
    }

    @GetMapping("/removeallcart/{idtable}")
    public String removeallcart(@PathVariable("idtable") Integer idtable) {
        cartService.removeAllProduct(idtable);
        return "redirect:/customer/viewcart/" + idtable;
    }

    @PostMapping("/datmon")
    public ResponseEntity<Invoice> datMon(@RequestParam("idtable") Integer idtable , @RequestParam("note") String note) {
        List<CartItem> cartItems = cartItemService.getAll(idtable);
        long total = 0;
        if (cartItems.size() >= 1) {
            Invoice invoicenew = new Invoice();
            invoicenew.setIdTable(idtable);
            invoicenew.setNote(note);
            Invoice invoice = invoiceService.saveInvoice(invoicenew);

            for (CartItem c : cartItems) {
                invoiceProductService.addProduct(c.getProduct(), invoice.getId(), c.getQuantity());
                total += c.getQuantity() * c.getProduct().getPrice();
            }
            invoice.setTotal(total);
            invoiceService.saveInvoice(invoice);
            messagingTemplate.convertAndSend("/topic/new-order", invoice);
            cartService.removeAllProduct(idtable);

            return ResponseEntity.ok(invoice);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping ("/viewtable/{id}")
    public String viewlTable(Model model, @PathVariable("id") Integer id) {
        List<Invoice> invoice = invoiceService.findInvoiceByIdTable(id);
        long temp = invoice.stream().mapToLong(Invoice::getTotal).sum();
        model.addAttribute("sumtotal", temp);
        model.addAttribute("invoices", invoice);
        model.addAttribute("idtable", id);
        return "customer-table";
    }

    @GetMapping("/detailtable/{id}")
    public String detailTable(Model model, @PathVariable("id") Integer id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        model.addAttribute("invoice", invoice);
        model.addAttribute("idtable", invoice.getIdTable());
        return "customer-detail-table";
    }

    @GetMapping("/help/{id}")
    public String help(Model model, @PathVariable("id") Integer id) {
        model.addAttribute("idtable", id);
        return "customer-help";
    }
    @GetMapping("/findfood")
    public String finFood(Model model, @RequestParam("name") String name,@RequestParam("id") Integer id){
        model.addAttribute("idtable", id);
        model.addAttribute("menu", productService.getProductByName(name));
        return "customer-menu";
    }
    @GetMapping("/showchat/{id}")
    public String showChatCustomer(Model model, @PathVariable("id") Integer id) {
        model.addAttribute("idtable", id);
        return "chat";
    }


}
