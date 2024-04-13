package nam.nguyen.store.controller;

import nam.nguyen.store.model.CalendarStaff;
import nam.nguyen.store.model.CalendarStaffResponse;
import nam.nguyen.store.model.Role;
import nam.nguyen.store.model.User;
import nam.nguyen.store.service.CalendarStaffService;
import nam.nguyen.store.service.RoleService;
import nam.nguyen.store.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private CalendarStaffService calendarStaffService;
    @GetMapping("/alluser")
    public String getAllUser(Model model){
        List<User> users = userService.getAllUser();
        model.addAttribute("users",users);
        return "admin-home";
    }
    @GetMapping("/adduser")
    public String viewaddUser(Model model){
        User user = new User();
        model.addAttribute("user",user);
        List<Role> roles = roleService.getAll();
        model.addAttribute("roles",roles);
        return "admin-add-user";
    }
    @PostMapping("/adduser")
    public String addUser(@ModelAttribute User user , @RequestParam("roleId") Integer roleId){
        user.setPassword(user.getPhone());
        user.setUsername(user.getPhone());
        userService.register(user,roleId);
        return "redirect:/admin/alluser";
    }
    @GetMapping("/removeuser/{iduser}")
    public String removeUser(@PathVariable ("iduser") Integer iduser){
        userService.removeUser(iduser);
        return "redirect:/admin/alluser";
    }
    @GetMapping("/calender")
    public String getCalender(Model model){
        List<User> users = userService.getAllUser();
        model.addAttribute("users",users);
        return "admin-calender";
    }
    @PostMapping("/calender/add")
    public @ResponseBody String addCalender(@RequestParam("username") String username, @RequestParam("start") String start, @RequestParam("end") String end){
        try {
            if (!username.isEmpty() && !start.isEmpty()){
                User user = userService.getUserByUserName(username);
                CalendarStaff calendarStaff= new CalendarStaff();
                calendarStaff.setStart(start);
                calendarStaff.setTitle(user.getName());
                calendarStaff.setEnd(end);
                calendarStaff.setUser(user);
                calendarStaff.setMinutes(getHours(start,end));
                calendarStaffService.addCalendarStaff(calendarStaff);
                return "Thêm thành công";
            }
            return "Vui lòng điền đủ";
        }catch (Exception e){
            return "Lỗi thêm";
        }
    }
    Long getHours(String thoigian1 , String thoigian2){
        LocalDateTime mocThoiGian1 = LocalDateTime.parse(thoigian1);
        LocalDateTime mocThoiGian2 = LocalDateTime.parse(thoigian2);
        Duration khoangCach = Duration.between(mocThoiGian1, mocThoiGian2);
        return khoangCach.toMinutes();

    }
    @GetMapping("/calender/get")
    public @ResponseBody List<CalendarStaffResponse> getAllCalendarStaff() {
        List<CalendarStaff> calendarStaffList = calendarStaffService.getAllCalendarStaff();


        List<CalendarStaffResponse> responseList = new ArrayList<>();
        for (CalendarStaff c:calendarStaffList) {
            CalendarStaffResponse calendarStaffResponse= new CalendarStaffResponse();
            calendarStaffResponse.setTitle(c.getTitle());
            calendarStaffResponse.setStart(c.getStart());
            calendarStaffResponse.setEnd(c.getEnd());
            responseList.add(calendarStaffResponse);
        }


        return responseList;
    }

}
