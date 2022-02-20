package com.liumulin.repository;

import com.liumulin.annotation.FirstLevelRepository;
import com.liumulin.annotation.SecondLevelRepository;

/**
 * @author liuqiang
 * @since 2022-02-18
 */
//@FirstLevelRepository(value = "firstLevelRepository")
@SecondLevelRepository(value = "firstLevelRepository")
public class MyFirstLevelRepository {
}
