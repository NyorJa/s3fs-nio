package org.carlspring.cloud.storage.s3fs.FileSystemProvider;

import org.carlspring.cloud.storage.s3fs.S3FileSystem;
import org.carlspring.cloud.storage.s3fs.S3FileSystemProvider;
import org.carlspring.cloud.storage.s3fs.S3UnitTestBase;
import org.carlspring.cloud.storage.s3fs.util.AmazonS3ClientMock;
import org.carlspring.cloud.storage.s3fs.util.AmazonS3MockFactory;
import org.carlspring.cloud.storage.s3fs.util.S3EndpointConstant;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.carlspring.cloud.storage.s3fs.AmazonS3Factory.ACCESS_KEY;
import static org.carlspring.cloud.storage.s3fs.AmazonS3Factory.SECRET_KEY;
import static org.junit.jupiter.api.Assertions.*;

public class DeleteTest
        extends S3UnitTestBase
{


    @BeforeEach
    public void setup()
            throws IOException
    {
        s3fsProvider = getS3fsProvider();
        fileSystem = s3fsProvider.newFileSystem(S3EndpointConstant.S3_GLOBAL_URI_TEST, null);
    }

    @Test
    public void deleteFile()
            throws IOException
    {
        // fixtures
        AmazonS3ClientMock client = AmazonS3MockFactory.getAmazonClientMock();
        client.bucket("bucketA").dir("dir").file("dir/file");

        // act
        Path file = createNewS3FileSystem().getPath("/bucketA/dir/file");
        s3fsProvider.delete(file);

        // assertions
        assertTrue(Files.notExists(file));
    }

    @Test
    public void deleteEmptyDirectory()
            throws IOException
    {
        // fixtures
        AmazonS3ClientMock client = AmazonS3MockFactory.getAmazonClientMock();
        client.bucket("bucketA").dir("dir");
        Path base = s3fsProvider.newFileSystem(URI.create("s3://endpoint1/"),
                                               ImmutableMap.<String, Object>builder().put(ACCESS_KEY, "access_key")
                                                                                     .put(SECRET_KEY, "secret_key")
                                                                                     .build()).getPath("/bucketA/dir");

        // act
        s3fsProvider.delete(base);

        // assert
        assertTrue(Files.notExists(base));
    }

    @Test
    public void deleteDirectoryWithEntries()
            throws IOException
    {
        // fixtures
        AmazonS3ClientMock client = AmazonS3MockFactory.getAmazonClientMock();
        client.bucket("bucketA").dir("dir").file("dir/file");

        Path file = createNewS3FileSystem().getPath("/bucketA/dir/file");

        Exception exception = assertThrows(DirectoryNotEmptyException.class, () -> {
            s3fsProvider.delete(file.getParent());
        });

        assertNotNull(exception);
    }

    @Test
    public void deleteFileNotExists()
            throws IOException
    {
        // fixtures
        AmazonS3ClientMock client = AmazonS3MockFactory.getAmazonClientMock();
        client.bucket("bucketA").dir("dir");

        Path file = createNewS3FileSystem().getPath("/bucketA/dir/file");

        Exception exception = assertThrows(NoSuchFileException.class, () -> {
            s3fsProvider.delete(file);
        });

        assertNotNull(exception);
    }

    /**
     * create a new file system for s3 scheme with fake credentials
     * and global endpoint
     *
     * @return FileSystem
     * @throws IOException
     */
    private S3FileSystem createNewS3FileSystem()
            throws IOException
    {
        try
        {
            return s3fsProvider.getFileSystem(S3EndpointConstant.S3_GLOBAL_URI_TEST);
        }
        catch (FileSystemNotFoundException e)
        {
            return (S3FileSystem) FileSystems.newFileSystem(S3EndpointConstant.S3_GLOBAL_URI_TEST, null);
        }
    }

}
