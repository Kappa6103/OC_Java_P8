package com.openclassrooms.tourguide;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

import static org.junit.jupiter.api.Assertions.*;

public class TestPerformance {

	/*
	 * A note on performance improvements:
	 * 
	 * The number of users generated for the high volume tests can be easily
	 * adjusted via this method:
	 * 
	 * InternalTestHelper.setInternalUserNumber(100000);
	 * 
	 * 
	 * These tests can be modified to suit new solutions, just as long as the
	 * performance metrics at the end of the tests remains consistent.
	 * 
	 * These are performance metrics that we are trying to hit:
	 * 
	 * highVolumeTrackLocation: 100,000 users within 15 minutes:
	 * assertTrue(TimeUnit.MINUTES.toSeconds(15) >=
	 * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 *
	 * highVolumeGetRewards: 100,000 users within 20 minutes:
	 * assertTrue(TimeUnit.MINUTES.toSeconds(20) >=
	 * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */
/*
with my own class 50_000 in 6 min 27 avec result in hasmap same for not casting
with the executor, but re-make one exe per thread, poua ! 12min7
3min42 -> with fixed size of 30 threads, pareil avec un wait in main threads
2 min 56 -> with a cached thread pools/ et sans la synchro 2m37;

for 100_000 -> 9 mins
sans le synchro
for 100_000 ->
 */
	@Test
	public void highVolumeTrackLocation() throws ExecutionException, InterruptedException {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		// Users should be incremented up to 100,000, and test finishes within 15
		// minutes
		final int NUM_OF_USERS = 100_000; //3m25
		InternalTestHelper.setInternalUserNumber(NUM_OF_USERS);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();
		Map<User, CompletableFuture<VisitedLocation>> addedLocation = new HashMap<>(NUM_OF_USERS);

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		for (User user : allUsers) {
			addedLocation.put(user, tourGuideService.trackUserLocation(user));
		}
		System.out.println("Step one done, all futures put in the hashmap");

		addedLocation.forEach((user, location) -> {
			assertTrue(user.getVisitedLocations().contains(location.join()));
		});
		assertEquals(NUM_OF_USERS, addedLocation.values().size());
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();
		tourGuideService.shutdownExecutorService();
		rewardsService.shutdownExecutorService();
		System.out.println("highVolumeTrackLocation: Time Elapsed: "
				+ TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	/* 3m36
	3m52 pour 100_000
	l'etat n'est pas a verifier ?
	 */
	@Test
	public void highVolumeGetRewards() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		// Users should be incremented up to 100,000, and test finishes within 20
		// minutes
		InternalTestHelper.setInternalUserNumber(100);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

		//allUsers.forEach(u -> rewardsService.calculateRewards(u));

		List<CompletableFuture<Void>> futures = allUsers.stream()
				.map(u -> rewardsService.calculateRewards(u))
				.toList();

		// Wait for all calculations to complete
		System.out.println("Step one done");

		futures.forEach(CompletableFuture::join);

		System.out.println("finished calculate rewards");


		for (User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
				+ " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

}
