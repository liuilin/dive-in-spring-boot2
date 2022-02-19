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
    @Override
    public Integer sum(Integer... values) {
        System.out.println("Java 8 Lambda 实现");
        return Stream.of(values).reduce(0, Integer::sum);
    }
}
