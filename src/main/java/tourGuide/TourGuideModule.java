package tourGuide;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.GpsUtil;
import org.springframework.context.annotation.Primary;
import rewardCentral.RewardCentral;
import tourGuide.service.GPSUtilService;
import tourGuide.service.RewardsService;

@Configuration
public class TourGuideModule {
	@Primary
	@Bean
	public GPSUtilService getGpsUtil() {
		return new GPSUtilService();
	}
	
	@Bean
	public RewardsService getRewardsService() {
		return new RewardsService(getGpsUtil(), getRewardCentral());
	}
	
	@Bean
	public RewardCentral getRewardCentral() {
		return new RewardCentral();
	}
	
}
