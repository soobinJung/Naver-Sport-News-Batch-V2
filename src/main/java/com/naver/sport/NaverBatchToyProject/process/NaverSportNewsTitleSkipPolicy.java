package com.naver.sport.NaverBatchToyProject.process;

import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class NaverSportNewsTitleSkipPolicy implements SkipPolicy {

    @Override
    public boolean shouldSkip(Throwable throwable, long skipCount) throws SkipLimitExceededException {

        throwable.printStackTrace();

        if(throwable instanceof DataIntegrityViolationException){
            return true;
        }

        return false;
    }
}
