package com.nguyenvanlinh.identityservice.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nguyenvanlinh.identityservice.dto.response.OutBoundUserResponse;

@FeignClient(name = "outbound-user-client", url = "https://www.googleapis.com/")
public interface OutBoundUserClient {
    @GetMapping("/oauth2/v1/userinfo")
    OutBoundUserResponse getUserInfo(@RequestParam("alt") String alt, @RequestParam("access_token") String accessToken);
}
