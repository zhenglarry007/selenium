package com.larry.data.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.larry.data.dynamic.BookingDataFactory;
import com.larry.model.Booking;
import com.larry.model.DashboardConfig;
import com.larry.model.DashboardTestData;
import com.larry.model.LoginTestData;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TestDataProvider {
    
    private static final int DYNAMIC_DATA_COUNT = 5;

    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final CsvMapper csvMapper = new CsvMapper();
    
    private static DashboardConfig dashboardConfig;

    private TestDataProvider() {
    }

    @DataProvider(name = "loginTestData")
    public static Object[][] loginTestData() {
        return loadJsonData("/testdata/login_test_data.json", new TypeReference<List<LoginTestData>>() {});
    }

    @DataProvider(name = "bookingDataJson")
    public static Object[][] bookingDataJson() {
        return loadJsonData("/testdata/booking.json", new TypeReference<List<Booking>>() {});
    }

    @DataProvider(name = "bookingDataDynamic")
    public static Object[][] bookingDataDynamic() {
        List<Booking> bookingList = new ArrayList<>();
        for (int i = 0; i < DYNAMIC_DATA_COUNT; i++) {
            Booking booking = BookingDataFactory.createBookingData();
            bookingList.add(booking);
        }
        return toObjectArray(bookingList);
    }

    @DataProvider(name = "dashboardTestData")
    public static Object[][] dashboardTestData() {
        return loadJsonData("/testdata/dashboard_test_data.json", new TypeReference<List<DashboardTestData>>() {});
    }
    
    public static DashboardConfig getDashboardConfig() {
        if (dashboardConfig == null) {
            dashboardConfig = loadJsonObject("/testdata/dashboard_test_data.json", DashboardConfig.class);
        }
        return dashboardConfig;
    }

    private static <T> Object[][] loadJsonData(String resourcePath, TypeReference<List<T>> typeReference) {
        try (InputStream inputStream = TestDataProvider.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            List<T> dataList = jsonMapper.readValue(inputStream, typeReference);
            return toObjectArray(dataList);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load JSON data from " + resourcePath, e);
        }
    }

    private static <T> Object[][] loadYamlData(String resourcePath, TypeReference<List<T>> typeReference) {
        try (InputStream inputStream = TestDataProvider.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            List<T> dataList = yamlMapper.readValue(inputStream, typeReference);
            return toObjectArray(dataList);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load YAML data from " + resourcePath, e);
        }
    }

    private static <T> Object[][] loadCsvData(String resourcePath, Class<T> clazz) {
        try (InputStream inputStream = TestDataProvider.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }

            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            
            List<T> dataList = new ArrayList<>();
            try (MappingIterator<T> iterator = csvMapper.readerFor(clazz).with(schema).readValues(inputStream)) {
                while (iterator.hasNext()) {
                    dataList.add(iterator.next());
                }
            }

            return toObjectArray(dataList);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load CSV data from " + resourcePath, e);
        }
    }
    
    private static <T> T loadJsonObject(String resourcePath, Class<T> clazz) {
        try (InputStream inputStream = TestDataProvider.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            return jsonMapper.readValue(inputStream, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load JSON object from " + resourcePath, e);
        }
    }

    private static <T> Object[][] toObjectArray(List<T> dataList) {
        Object[][] data = new Object[dataList.size()][1];
        for (int i = 0; i < dataList.size(); i++) {
            data[i][0] = dataList.get(i);
        }
        return data;
    }
}
