package com.liumulin.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

/**
 * @author liuqiang
 * @since 2022-02-19
 */
@Profile("Java8")
@Service
public class Java8CalculateService implements CalculateService {

    public static void main(String[] args) {
        System.out.println(new Java8CalculateService().sum(1, 2, 3, 4, 5, 6, 7, 8, 9));
    }

    @Override
    public Integer sum(Integer... values) {
        System.out.println("Java 8 Lambda 实现");
        return Stream.of(values).reduce(0, Integer::sum);
    }
}
