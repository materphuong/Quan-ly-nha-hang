package nam.nguyen.store.controller;

import nam.nguyen.store.model.Category;
import nam.nguyen.store.model.Product;
import nam.nguyen.store.service.CategoryService;
import nam.nguyen.store.service.DiningTableService;
import nam.nguyen.store.service.InvoiceService;
import nam.nguyen.store.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/store")
public class StoreContoller {
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DiningTableService tableService;
    @GetMapping("/editfood/{id}")
    public  String editFood(Model model , @PathVariable("id") Integer id){
        model.addAttribute("food",productService.getProductById(id));
        model.addAttribute("category",categoryService.getAllCategorys());
        return "store-edit-food";
    }
    @PostMapping("/updatefood")
    public String editFood(@ModelAttribute Product product, @RequestParam("file") MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                String fileName = "/images/products/" + file.getOriginalFilename();
                Path path = Paths.get("src/main/resources/static" + fileName);
                Files.write(path, file.getBytes());
                product.setUrl(fileName);
            }
            productService.saveProduct(product);
            return "redirect:/staff/viewmenu";
        } catch (IOException e) {
            // Xử lý lỗi khi lưu tệp
            e.printStackTrace();
            return "errorPage";
        }
    }
    @GetMapping("/addfood")
    public  String addFood(Model model ){
        Product product = new Product();
        model.addAttribute("food",product);
        model.addAttribute("category",categoryService.getAllCategorys());
        return "staff-addfood";
    }

    @PostMapping("/addfood")
    public String addFood(@ModelAttribute Product product, @RequestParam("file") MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                String fileName = "/images/products/" + file.getOriginalFilename();
                Path path = Paths.get("src/main/resources/static" + fileName);
                Files.write(path, file.getBytes());
                product.setUrl(fileName);
            }
            product.setStatussell(0);
            productService.saveProduct(product);
            return "redirect:/staff/viewmenu";
        } catch (IOException e) {
            // Xử lý lỗi khi lưu tệp
            e.printStackTrace();
            return "errorPage";
        }
    }
    @GetMapping("/foodstatus/{id}")
    public  String removeFood(@PathVariable("id") Integer id){
        Product product =productService.getProductById(id);
        if (product.getStatussell()==0){
            product.setStatussell(1);

        }else {
            product.setStatussell(0);
        }
        productService.saveProduct(product);
        return "redirect:/staff/viewmenu";
    }
    @GetMapping("/addtable/{quantity}")
    public String setTable(@PathVariable("quantity") Integer quantity) {
        tableService.setTable(quantity);
        return "redirect:/customer/home";
    }
    @GetMapping("/findfood")
    public String finFood(Model model, @RequestParam("name") String name){
        model.addAttribute("menu", productService.getProductByName(name));
        model.addAttribute("categorys",categoryService.getAllCategorys());
        return "staff-menu";
    }
    @GetMapping("/getfoodbycategory")
    public String getFoodByCategory(Model model, @RequestParam("idcategory") Integer idcategory){
        Category category = categoryService.getCategoryById(idcategory);
        model.addAttribute("menu",category.getProducts());
        model.addAttribute("categorys",categoryService.getAllCategorys());
        return "staff-menu";
    }
    @GetMapping("/getfoodbystatussell")
    public String getFoodByStatusSell(Model model, @RequestParam("status") Integer status){
        List<Product> products = productService.getAllProductForCustomer(status);
        model.addAttribute("categorys",categoryService.getAllCategorys());
        model.addAttribute("menu",products);
        return "staff-menu";
    }
}
