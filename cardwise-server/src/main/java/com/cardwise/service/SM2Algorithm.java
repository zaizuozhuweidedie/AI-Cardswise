package com.cardwise.service;

import com.cardwise.dto.SM2ResultResponse;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class SM2Algorithm {

    public SM2ResultResponse calculate(int quality, double easeFactor, int intervalDays, int repetitions) {
        if (quality < 1 || quality > 4) {
            throw new IllegalArgumentException("Quality must be between 1 and 4");
        }

        int newRepetitions;
        int newInterval;

        if (quality < 3) {
            newRepetitions = 0;
            newInterval = 1;
        } else {
            newRepetitions = repetitions + 1;
            if (newRepetitions == 1) {
                newInterval = 1;
            } else if (newRepetitions == 2) {
                newInterval = 6;
            } else {
                newInterval = (int) Math.round(intervalDays * easeFactor);
            }
        }

        double newEase = easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        if (newEase < 1.3) {
            newEase = 1.3;
        }

        LocalDateTime nextReviewAt = LocalDateTime.now().plusDays(newInterval);

        return new SM2ResultResponse(newEase, newInterval, newRepetitions, nextReviewAt);
    }
}
