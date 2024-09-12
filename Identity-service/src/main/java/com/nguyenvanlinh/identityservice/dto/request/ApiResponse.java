package com.nguyenvanlinh.identityservice.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    // mã
    @Builder.Default
    private int code = 1000; // defaul code khi thành công
    //
    private String message;
    // generate type gì có nhiều kểu kết quả trả về
    private T result;
}
