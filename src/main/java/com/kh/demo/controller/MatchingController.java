package com.kh.demo.controller;

import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kh.demo.domain.dto.*;
import com.kh.demo.service.BoardService;
import com.kh.demo.service.ChallengeService;
import com.kh.demo.service.TrainerMatchingService;
import com.kh.demo.service.TrainerService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/matching/*")
public class MatchingController {

    @Autowired
    private ChallengeService challService;

    @Autowired
    @Qualifier("TrainerServiceImpl")
    private TrainerService tservice;

    @Autowired @Qualifier("BoardServiceImpl")
    private BoardService boardservice;

    @Autowired
    @Qualifier("TrainerMatchingServiceImpl")
    private TrainerMatchingService MatchingService;

    @GetMapping("matching_list")
    public void matching_list(Criteria cri, Model model) throws Exception {
        List<TrainerMatchingBoardDTO> list = MatchingService.getmatchingList(cri);
        model.addAttribute("list", list);

       /* model.addAttribute("review_cnt_list",MatchingService.getReviewCntList(list));*/
    }

    @GetMapping("thumbnail")
    public ResponseEntity<Resource> thumbnail(String sysName) throws Exception{
        return MatchingService.getThumbnailResource(sysName);
    }

    @GetMapping("matching_write")
    public void write(@ModelAttribute("cri") Criteria cri,Model model) {
        System.out.println(cri);
    }

    @PostMapping("matching_write")
    public String write(TrainerMatchingBoardDTO board, Criteria cri) throws Exception{
        Long boardnum = 0l;
        if(MatchingService.regist(board)) {
            boardnum = MatchingService.getLastNum(board.getTrainerId());
            return "redirect:/matching/matching_view"+cri.getListLink()+"&boardnum="+boardnum;
        }
        else {
            return "redirect:/matching/matching_list"+cri.getListLink();
        }
    }



    @GetMapping("/matching/matching_view")
    public String get(@RequestParam Long boardNum, HttpServletRequest req, HttpServletResponse resp, Model model) {
        List<TrainerMatchingBoardDTO> list = MatchingService.boardView(boardNum);

        for (TrainerMatchingBoardDTO board : list) {

            ProfileDTO profileInfo = MatchingService.getProfileInfo(board.getTrainerId());
            ProfileDTO careerInfo = MatchingService.getCareerInfo(board.getTrainerId());
            TrainerDTO trainerInfo = MatchingService.getTrainerInfo(board.getTrainerId());

            model.addAttribute("profileInfo", profileInfo);
            model.addAttribute("careerInfo",careerInfo);
            model.addAttribute("trainerInfo",trainerInfo);
        }
        model.addAttribute("list", list);


        if (list == null) {
            return "error";
        }

        HttpSession session = req.getSession();
        model.addAttribute("list", list);

        // foodNum에 대한 조회수 증가 처리
        Cookie[] cookies = req.getCookies();
        boolean hasViewed = false;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("view_board" + boardNum)) {
                    hasViewed = true;
                    break;
                }
            }
        }

        if (!hasViewed) {
            // 조회수 증가
            MatchingService.updateViewCount(boardNum);

            // "view_food{foodNum}" 이름의 쿠키(유효기간: 3600초)를 생성해서 클라이언트 컴퓨터에 저장
            Cookie cookie = new Cookie("view_food" + boardNum, "viewed");
            cookie.setMaxAge(3600);
            resp.addCookie(cookie);
        }

        return "matching/matching_view";
    }

    @PostMapping("profileModal")
    @ResponseBody
    public String reportModal(@RequestParam("trainerNickname") String trainerNickname, HttpServletRequest req) throws Exception {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        Object userInfo = MatchingService.getUserByNickname(trainerNickname);

        HttpSession session = req.getSession();
        String loginUser_userId = (String) session.getAttribute("loginUser");

        if (userInfo instanceof TrainerDTO) {
            TrainerDTO trainerDTO = (TrainerDTO) userInfo;
            json.putPOJO("trainerDTO", trainerDTO);
            json.putPOJO("loginUser_userId", loginUser_userId);
        }
        else if (userInfo instanceof UserDTO) {
            UserDTO userDTO = (UserDTO) userInfo;
            json.putPOJO("userDTO", userDTO);
            json.putPOJO("loginUser_userId", loginUser_userId);
        }
        else {
            json.put("noData", "noData");
        }
        return json.toString();
    }
    @PostMapping("send_message")
    @ResponseBody
    public String u_t_matching(@RequestParam("receiveId") String receiveId, @RequestParam("sendId") String sendId, @RequestParam("contents") String contents) throws Exception {
        MessageDTO newMessage = new MessageDTO();
        newMessage.setReceiveId(receiveId);
        newMessage.setSendId(sendId);
        newMessage.setMessageContent(contents);


        MatchingService.saveMessage(newMessage);

        return "success"; // 적절한 응답 메시지
    }
    @PostMapping("u_t_matchModal")
    @ResponseBody
    public String matchingModal(@RequestParam("trainerId") String trainerId, HttpServletRequest req) throws Exception {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        HttpSession session = req.getSession();
        String userId = (String) session.getAttribute("loginUser");
        // Retrieve board information by trainerId
        TrainerMatchingBoardDTO boardInfo = MatchingService.getBoardBytrainerId(trainerId);
        System.out.println(boardInfo);
        UTMatchingDTO utInfo = MatchingService.getutBytrainerId(trainerId);
        System.out.println(utInfo);
        if (boardInfo instanceof TrainerMatchingBoardDTO) {
            TrainerMatchingBoardDTO trainerMatchingBoardDTO = (TrainerMatchingBoardDTO) boardInfo;
            json.putPOJO("trainerMatchingBoardDTO", trainerMatchingBoardDTO);
            System.out.println(trainerMatchingBoardDTO);
            // Retrieve userId from the session
            System.out.println(userId);
            // Add userId to the JSON response
            json.putPOJO("userId", userId);

            if (utInfo != null) {
                UTMatchingDTO utMatchingDTO = (UTMatchingDTO) utInfo;
                json.putPOJO("utMatchingDTO", utMatchingDTO);
                System.out.println(utInfo);
            }
        } else {
            json.put("noData", "noData");
        }

        // Add trainerId to the JSON response
        json.put("trainerId", trainerId);
        System.out.println("JSON Response: " + json.toString());
        return json.toString();
    }

    @PostMapping("apply")
    @ResponseBody
    public String u_t_matching(@RequestParam("userId") String userId, @RequestParam("trainerId") String trainerId) throws Exception {
        UTMatchingDTO newMatching = new UTMatchingDTO();
        newMatching.setUserId(userId);
        newMatching.setTrainerId(trainerId);


        MatchingService.saveMatching(newMatching);

        return "success";
    }

    @GetMapping("totalSearch")
    public void search(String keyword,Model model){
//        System.out.println(keyword);

        //인기게시글 띄우기
        List<BoardDTO> boardTop5List = boardservice.getBoardTop5List();

        // 트레이너 랭킹
        List<TrainerDTO> trainerTop5List= tservice.getTrainerTop5List();
        //전체 보드 게시글 수 찾기
        Long boardAllCnt = boardservice.getAllsearchCnt(keyword);
        System.out.println(boardAllCnt);

        //각 게시판에서 글 가져오기

        List<BoardDTO> infoSearchList = boardservice.getinfoSearchList(keyword);

        List<BoardDTO> tipSearchList = boardservice.getTipSearchList(keyword);
        List<BoardDTO> commuSearchList = boardservice.getCommuSearchList(keyword);

        List<BoardDTO> matchingSearchList = MatchingService.getMachingSearchList(keyword);
        List<ChallNoticeBoardDTO> challSearchList = challService.getChallSearchList(keyword);

        int[] boardCntArr ={infoSearchList.size(),
                tipSearchList.size(),commuSearchList.size(),matchingSearchList.size(),challSearchList.size()};

//        model.addAttribute("newsSearchList",newsSearchList);
//        model.addAttribute("exerSearchList",exerSearchList);
//        model.addAttribute("foodSearchList",foodSearchList);
        model.addAttribute("infoSearchList",infoSearchList);
        model.addAttribute("tipSearchList",tipSearchList);
        model.addAttribute("commuSearchList",commuSearchList);
        model.addAttribute("matchingSearchList",matchingSearchList);
        model.addAttribute("challSearchList",challSearchList);
        model.addAttribute("boardAllCnt",boardAllCnt);
        model.addAttribute("trainerTop5List",trainerTop5List);
        model.addAttribute("boardTop5List",boardTop5List);
        model.addAttribute("boardCntArr",boardCntArr);


    }

    @PostMapping("subscribe_check")
    public String checkSubscription(@RequestParam("sendId") String userId, @RequestParam("trainerId") String trainerId) {
        SubscribeDTO newSubscribe = new SubscribeDTO();
        newSubscribe.setUserId(userId);
        newSubscribe.setTrainerId(trainerId);
        System.out.println("checkSubs"+newSubscribe);
        SubscribeDTO isSubscribed = MatchingService.checkSubs(newSubscribe);

        if (isSubscribed != null) {
            return "subscribed";
        } else {
            return "unsubscribed";
        }
    }
    @PostMapping("subscribe_click")
    public String clickSubscription(@RequestParam("sendId") String userId, @RequestParam("trainerId") String trainerId) {
        SubscribeDTO newSubscribe = new SubscribeDTO();
        newSubscribe.setUserId(userId);
        newSubscribe.setTrainerId(trainerId);

        System.out.println("clickSubs"+newSubscribe);
        SubscribeDTO isSubscribed = MatchingService.clickSubs(newSubscribe);

        if (isSubscribed != null) {
            return "subscribed";
        } else {
            return "unsubscribed";
        }
    }


}
