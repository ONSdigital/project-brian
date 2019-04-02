package com.github.onsdigital.brian.handlers;

import com.github.onsdigital.brian.readers.DataSetReader;
import com.github.onsdigital.content.page.statistics.data.timeseries.TimeSeries;
import com.google.gson.GsonBuilder;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * A {@link Route} implementation that handles POST requests convert the uploaded .csdb file to a JSON representaion
 * of a Time series data set.
 */
public class CsdbHandler implements Route {

    private TimeSeriesConverter converter;
    private FileUploadHelper fileUploadHelper;
    private Supplier<SecretKey> encryptionKeySupplier;
    private DataSetReader dataSetReader;

    /**
     * Constuct a new CSDB API handler.
     *
     * @param fileUploadHelper      a helper to take care of getting the uploaded file from the HTTP request.
     * @param converter             the services that generates the Timesseries from the CSDB file.
     * @param encryptionKeySupplier provides {@link SecretKey} to use.
     * @param dataSetReader         a parser responsible for reading the uploaded CSDB file.
     */
    public CsdbHandler(FileUploadHelper fileUploadHelper, TimeSeriesConverter converter,
                       Supplier<SecretKey> encryptionKeySupplier, DataSetReader dataSetReader) {
        this.fileUploadHelper = fileUploadHelper;
        this.converter = converter;
        this.encryptionKeySupplier = encryptionKeySupplier;
        this.dataSetReader = dataSetReader;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        SecretKey key = encryptionKeySupplier.get();
        Path uploadFilePath = fileUploadHelper.getFileUploadPath(request.raw(), key);

        List<TimeSeries> timeSeries = converter.convert(uploadFilePath, dataSetReader, key);
        info().data("upload_file_path", uploadFilePath.toString())
                .data("time_series_size", timeSeries.size())
                .log("handle CSDB request completed successfully");

        writeResultToFile(timeSeries, true);
        return timeSeries;
    }

    void writeResultToFile(List<TimeSeries> result, boolean isSpark) throws IOException {
        Path dest = null;
        if (isSpark) {
            dest = Paths.get("/Users/dave/IdeaProjects/project-brian/spark.json");
        } else {
            dest = Paths.get("/Users/dave/IdeaProjects/project-brian/spark.json");
        }

        Files.write(dest, new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(result).getBytes(), CREATE, TRUNCATE_EXISTING);
    }


}

