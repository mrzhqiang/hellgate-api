package com.github.mrzhqiang.hellgate.stage;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StageService {

    private final StageRepository stageRepository;

    public StageService(StageRepository stageRepository) {
        this.stageRepository = stageRepository;
    }

    public List<Stage> listByRecommend() {
        return stageRepository.findAll(Sort.by(Sort.Order.desc("recommend")));
    }
}
