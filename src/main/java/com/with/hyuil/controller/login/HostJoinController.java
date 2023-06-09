package com.with.hyuil.controller.login;

import com.with.hyuil.config.auth.CustomUserDetails;
import com.with.hyuil.dto.users.BusinessDto;
import com.with.hyuil.dto.users.UserCodeDto;
import com.with.hyuil.dto.users.UserIdDto;
import com.with.hyuil.dto.users.UsersDto;
import com.with.hyuil.model.BusinessVo;
import com.with.hyuil.model.UsersVo;
import com.with.hyuil.model.enumaration.Type;
import com.with.hyuil.service.interfaces.EmailService;
import com.with.hyuil.service.interfaces.UsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;


@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/host")
public class HostJoinController {

    private final UsersService usersService;
    private final EmailService emailService;

    @GetMapping
    public String hostMain(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        try {
            model.addAttribute("userId", userDetails.getUsername());
            model.addAttribute("role", userDetails.getAuthorities().toString());
        } catch (NullPointerException e) {
            return "host/host";
        }
        return "host/host";
    }

    @GetMapping("/loginForm")
    public String loginHost(HttpServletRequest request, HttpServletResponse response) {
        StringBuffer requestURL = request.getRequestURL();
        request.setAttribute("requestURL", requestURL);
        return "host/hostLoginForm";
    }

    @GetMapping("/join")
    public String joinForm() {
        return "host/hostJoinForm";
    }

    @ResponseBody
    @PostMapping("/join/id")
    public String joinUser(@RequestBody UserIdDto userIdDto) {
        return String.valueOf(usersService.idCheck(userIdDto));
    }

    @PostMapping("/join/email")
    public String joinEmail(@ModelAttribute UsersDto usersDto, HttpSession session) {
        String randomCode = emailService.codeMailSend("WITH HYUIL 가입 인증", usersDto.getEmail());
        session.setAttribute("usersDto", usersDto);
        session.setAttribute("randomCode", randomCode);
        return "host/hostJoinEmailSend";
    }
    @ResponseBody
    @PostMapping("/join/accountValid")
    public String accountValid(@RequestBody BusinessDto businessDto, HttpSession session) {

        List<BusinessVo> businessVos = usersService.accountValid(businessDto);
        if (businessVos.size() != 0) {
            return "중복";
        }
        return "중복 아님";
    }

    @PostMapping("/join/emailCode")
    public String joinEmail(@ModelAttribute UserCodeDto userCodeDto, HttpSession session) {
        String randomCode = session.getAttribute("randomCode").toString();
        if(randomCode.equals(userCodeDto.getRandomCode())) {
            UsersDto usersDto = (UsersDto) session.getAttribute("usersDto");
            UsersVo usersVo = new UsersVo(usersDto);
            usersVo.hostUser(usersDto);
            usersVo.userType(Type.HOST);
            usersService.saveHost(usersVo);
            sessionRemoveCodeAndDto(session);

            log.info("New 호스트 회원 가입 = {}", usersDto);
            return "host/hostJoinComplete";
        }
        sessionRemoveCodeAndDto(session);
        return "host/hostJoinError";
    }

    private void sessionRemoveCodeAndDto(HttpSession session) {
        session.removeAttribute("randomCode");
        session.removeAttribute("userDto");
    }

}