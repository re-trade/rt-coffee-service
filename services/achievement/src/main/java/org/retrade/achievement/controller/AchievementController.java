package org.retrade.achievement.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.achievement.service.AchievementService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("achievements")
public class AchievementController {
    private final AchievementService achievementService;


}
