package com.liumulin.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * @author liuqiang
 * @since 2022-02-19
 */
@Profile("Java7")
@Service
public class Java7CalculateService implements CalculateService {

    public static void main(String[] args) {
        Java7CalculateService java7CalculateService = new Java7CalculateService();
        Integer sum = java7CalculateService.sum(1, 2, 3, 4, 5, 6, 7, 8, 9);
        System.out.println("sum = " + sum);
    }

    @Override
    public Integer sum(Integer... values) {
        System.out.println("Java 7 for 循环实现");
        int sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }
        return sum;
    }
}
