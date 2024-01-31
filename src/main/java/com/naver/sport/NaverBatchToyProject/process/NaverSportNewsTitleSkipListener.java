package com.naver.sport.NaverBatchToyProject.process;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.SkipListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NaverSportNewsTitleSkipListener<T, S> implements SkipListener<T, S> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void onSkipInRead(Throwable t) {
        jdbcTemplate.update("INSERT INTO NAVER_SPORT_NEWS_TITLE_ERROR (error) VALUES(?)", t.getMessage());
    }

    @Override
    public void onSkipInWrite(S item, Throwable t) {
        jdbcTemplate.update("INSERT INTO NAVER_SPORT_NEWS_TITLE_ERROR (error) VALUES(?)", t.getMessage());
    }
}
