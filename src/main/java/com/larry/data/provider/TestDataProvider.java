package com.larry.data.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.larry.model.Booking;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TestDataProvider {

    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final CsvMapper csvMapper = new CsvMapper();

    private TestDataProvider() {
    }

    @DataProvider(name = "bookingDataJson")
    public static Object[][] bookingDataJson() {
        return loadJsonData("/testdata/booking.json", new TypeReference<List<Booking>>() {});
    }

    @DataProvider(name = "bookingDataYaml")
    public static Object[][] bookingDataYaml() {
        return loadYamlData("/testdata/booking.yaml", new TypeReference<List<Booking>>() {});
    }

    @DataProvider(name = "bookingDataCsv")
    public static Object[][] bookingDataCsv() {
        return loadCsvData("/testdata/booking.csv", Booking.class);
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

    private static <T> Object[][] toObjectArray(List<T> dataList) {
        Object[][] data = new Object[dataList.size()][1];
        for (int i = 0; i < dataList.size(); i++) {
            data[i][0] = dataList.get(i);
        }
        return data;
    }
}
