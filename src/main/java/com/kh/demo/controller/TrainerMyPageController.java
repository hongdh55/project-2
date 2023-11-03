package com.kh.demo.controller;

import com.kh.demo.domain.dto.TrainerDTO;
import com.kh.demo.domain.dto.UserDTO;
import com.kh.demo.service.TrainerMyPageService;
import com.kh.demo.service.UserMyPageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/trainermypage/*")
public class TrainerMyPageController {

    @Autowired
    @Qualifier("TrainerMyPageServiceImpl")
    private TrainerMyPageService service;



    @GetMapping("trainer_challenge")
    public void replaceChallenge(){}


    @GetMapping("trainer_modify")
    public void trainer_modify(HttpServletRequest req, Model model) {
        HttpSession session = req.getSession();
        String loginUser = (String) session.getAttribute("loginUser");
        TrainerDTO user = service.getUserDetail(loginUser);
        model.addAttribute("user", user);
    }

    @GetMapping("trainer_myinfo_modify")
    public void trainer_myinfo_modify(HttpServletRequest req, Model model){
        HttpSession session = req.getSession();
        String loginUser = (String) session.getAttribute("loginUser");
        TrainerDTO user = service.getUserDetail(loginUser);
        model.addAttribute("user", user);
    }


    @PostMapping("trainer_myinfo_modify")
    public String trainer_myinfo_modify(TrainerDTO trainerdto, Model model) {
        System.out.println(trainerdto);
        if (service.user_modify(trainerdto)){
            TrainerDTO user = service.getUserDetail(trainerdto.getTrainerId());
            model.addAttribute("user", user);
            return "redirect:/trainermypage/trainer_myinfo";
        }
        else {
            return "redirect:/";
        }
    }


}
