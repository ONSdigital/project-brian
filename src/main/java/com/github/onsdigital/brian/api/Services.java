package com.github.onsdigital.brian.api;

import com.github.davidcarboni.cryptolite.Crypto;
import com.github.davidcarboni.cryptolite.Keys;
import com.github.davidcarboni.fileupload.encrypted.EncryptedFileItemFactory;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.brian.data.ErrorResponse;
import com.github.onsdigital.brian.data.TimeSeriesDataSet;
import com.github.onsdigital.brian.data.TimeSeriesObject;
import com.github.onsdigital.brian.exception.BrianException;
import com.github.onsdigital.brian.publishers.TimeSeriesPublisher;
import com.github.onsdigital.brian.readers.DataSetReader;
import com.github.onsdigital.brian.readers.DataSetReaderCSDB;
import com.github.onsdigital.brian.readers.DataSetReaderCSV;
import com.github.onsdigital.content.page.statistics.data.timeseries.TimeSeries;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpStatus;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

/**
 * Created by thomasridd on 08/06/15.
 */
@Api
public class Services {

    @POST
    public Object postToServices(HttpServletRequest request,
                                           HttpServletResponse response) throws IOException, FileUploadException {

        info().log("service endpoint: request received");

        String[] segments = request.getPathInfo().split("/");

        try {
            if (segments.length > 2 && segments[2].equalsIgnoreCase("ConvertCSDB")) {
                // Convert with CSDB Reader
                List<TimeSeries> result = convert(request, response, new DataSetReaderCSDB());
                info().log("returning time series from CSDB reader");
                return result;
            } else if (segments.length > 2 && segments[2].equalsIgnoreCase("ConvertCSV")) {
                // Convert with CSV Reader
                List<TimeSeries> result = convert(request, response, new DataSetReaderCSV());
                info().log("returning time series from CSV reader");
                return result;
            } else {
                info().log("returning 400 BAD_REQUEST");
                response.setStatus(HttpStatus.BAD_REQUEST_400);
                return new ErrorResponse(HttpStatus.BAD_REQUEST_400,"invalid endpoint called");
            }

        } catch (BrianException e) {
            error().logException(e, "failed to convert time series file");
            response.setStatus(e.getHttpStatus());
            return new ErrorResponse(e.getHttpStatus(),e.getMessage());
        } catch (Exception e) {
            error().logException(e, "internal server error");
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR_500,"internal server error");
        }
    }


    /**
     * Respond to a Service endpoint using a specific DataSetReader
     *
     * @param request  a MultiPartRequest to the Service endpoint with a file upload
     * @param response the response (to which we write a List<TimeSeries> object)
     * @param reader   a reader used to run the conversion
     * @throws IOException
     * @throws FileUploadException
     */
    private List<TimeSeries> convert(HttpServletRequest request,
                                     HttpServletResponse response,
                                     DataSetReader reader) throws IOException, FileUploadException, BrianException {
        // Get the input file
        SecretKey key = Keys.newSecretKey();
        Path dataFile = getFileFromMultipartRequest(request, key);

        if (dataFile != null) {

            List<TimeSeries> convertedSeries = getTimeSeries(reader, key, dataFile);

            return convertedSeries;


        } else {
            response.setStatus(HttpStatus.BAD_REQUEST_400);
            return null;
        }
    }

    /**
     * Get the timeseries list from a data file
     *
     * @param reader   the reader used to extract a dataset
     * @param key      the secretkey used to encrypt the data file on disk
     * @param dataFile the datafile
     * @return
     * @throws IOException
     */
    private List<TimeSeries> getTimeSeries(DataSetReader reader, SecretKey key, Path dataFile) throws IOException, BrianException {

        // Convert datafile to dataSet using favoured reader
        TimeSeriesDataSet timeSeriesDataSet = reader.readFile(dataFile, key);

        // Extract as brian timeseries
        List<TimeSeriesObject> brianSeries = new ArrayList<TimeSeriesObject>(timeSeriesDataSet.timeSeries.values());

        // Convert to zebedee timeseries
        List<TimeSeries> contentSeries = TimeSeriesPublisher.convertToContentLibraryTimeSeriesList(brianSeries);

        return contentSeries;
    }

    private Path getFileFromMultipartRequest(HttpServletRequest request, SecretKey key) throws IOException, FileUploadException {
        if (!ServletFileUpload.isMultipartContent(request)) {
            return null;
        }

        // Create a factory for disk-based file items
        EncryptedFileItemFactory factory = new EncryptedFileItemFactory();

        // Configure a repository (to ensure a secure temp location is used)
        File repository = Files.createTempDirectory("csdb").toFile();
        factory.setRepository(repository);

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Read the request to a temporary file and return
        List<FileItem> items = upload.parseRequest(request);
        for (FileItem item : items) {
            if (!item.isFormField()) {
                Path tempFile = Files.createTempFile("csdb", ".csdb");
                try (OutputStream stream = new Crypto().encrypt(Files.newOutputStream(tempFile), key)) {
                    IOUtils.copy(item.getInputStream(), stream);
                }
                return tempFile;
            }
        }
        return null;
    }
}
