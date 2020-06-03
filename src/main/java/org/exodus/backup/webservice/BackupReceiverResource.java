package org.exodus.backup.webservice;

import org.apache.commons.io.IOUtils;
import org.exodus.backup.service.FileService;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Path("receiver")
public class BackupReceiverResource {

    private static final Logger LOG = LoggerFactory.getLogger(BackupReceiverResource.class);

    @Inject
    private FileService fileService;

    @GET
    public Response isUp() {
        return Response.ok(LocalDateTime.now().toString()).build();
    }

    @POST
    @Path("{folder}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@PathParam("folder") String folder, MultipartFormDataInput input) {

        if(!fileService.folderExists(folder)){
            LOG.error(String.format("UPLOAD.FAILED folder with name %s dont exist", folder));
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("file");

        inputParts.forEach(inputPart -> {

            MultivaluedMap<String, String> headers = inputPart.getHeaders();
            String fileName = getFileName(headers);

            try {
                InputStream inputStream = inputPart.getBody(InputStream.class, null);
                byte[] bytes = IOUtils.toByteArray(inputStream);
                fileService.store(folder, fileName, bytes);
            } catch (IOException e) {
                LOG.error(String.format("FILE.READ.FAILED failed to read or store content of file %s", fileName), e);
            }
        });

        return Response.ok().build();

    }

    private String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                return name[1].trim().replaceAll("\"", "");
            }
        }
        return "unknown";
    }

}
