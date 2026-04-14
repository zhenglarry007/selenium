package com.larry.data.dynamic;

import com.larry.enums.RoomType;
import com.larry.model.Booking;
import net.datafaker.Faker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

import static com.larry.config.ConfigurationManager.configuration;

public final class BookingDataFactory {

    private static final Faker faker = new Faker(new Locale.Builder().setLanguageTag(configuration().faker()).build());
    private static final Logger logger = LogManager.getLogger(BookingDataFactory.class);

    private BookingDataFactory() {
    }

    public static Booking createBookingData() {
        var booking = new Booking.BookingBuilder().
            email(faker.internet().emailAddress()).
            country(returnRandomCountry()).
            password(faker.credentials().password()).
            dailyBudget(returnDailyBudget()).
            newsletter(faker.bool().bool()).
            roomType(faker.options().option(RoomType.class)).
            roomDescription(faker.lorem().paragraph()).
            build();

        logger.info(booking);
        return booking;
    }

    private static String returnRandomCountry() {
        return faker.options().option("Beijing", "Shanghai", "Guangzhou", "Shenzhen");
    }

    private static String returnDailyBudget() {
        return faker.options().option("Under ￥100", "￥100 - ￥200", "￥200 - ￥400", "Over ￥400");
    }
}
