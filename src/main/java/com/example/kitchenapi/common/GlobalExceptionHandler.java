package com.example.kitchenapi.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

/**
 * Kitchen APIのグローバル例外ハンドラー
 * アプリケーション全体の例外を処理し、ProblemDetailレスポンス（RFC 7807）を返します。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * リクエストボディの検証エラーを処理します。
     * @param ex MethodArgumentNotValidException
     * @return 400 Bad Requestステータスを持つProblemDetail
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation Failed");

        // すべての検証エラーを収集
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        problemDetail.setDetail(errors);
        return problemDetail;
    }

    /**
     * コントローラーからスローされたResponseStatusExceptionを処理します。
     * @param ex ResponseStatusException
     * @return 適切なステータスコードを持つProblemDetail
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(ResponseStatusException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(ex.getStatusCode());
        problemDetail.setTitle(ex.getStatusCode().toString());
        problemDetail.setDetail(ex.getReason());
        return problemDetail;
    }

    /**
     * IllegalArgumentExceptionを処理します。
     * @param ex IllegalArgumentException
     * @return 400 Bad Requestステータスを持つProblemDetail
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Bad Request");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    /**
     * その他すべての未処理の例外を処理します。
     * @param ex Exception
     * @return 500 Internal Server Errorステータスを持つProblemDetail
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setDetail("An unexpected error occurred: " + ex.getMessage());
        return problemDetail;
    }
}
