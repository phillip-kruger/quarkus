package io.quarkus.vertx.http.runtime.devmode.ui;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.quarkus.fs.util.ZipUtils;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.impl.HttpUtils;
import io.vertx.core.net.impl.URIDecoder;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.Utils;

/**
 * A Handler to serve static files from jar files or from a local directory.
 */
public class DevUIStaticHandler implements Handler<RoutingContext> {

    private String jarFileUri;
    private String relativeRootDirectory;
    private String relativeRootPath;

    public DevUIStaticHandler() {

    }

    public DevUIStaticHandler(String jarFileUri, String relativeRootDirectory, String relativeRootPath) {
        this.jarFileUri = jarFileUri;
        this.relativeRootDirectory = relativeRootDirectory;
        this.relativeRootPath = relativeRootPath;
    }

    public String getJarFileUri() {
        return jarFileUri;
    }

    public void setJarFileUri(String jarFileUri) {
        this.jarFileUri = jarFileUri;
    }

    public String getRelativeRootDirectory() {
        return relativeRootDirectory;
    }

    public void setRelativeRootDirectory(String relativeRootDirectory) {
        this.relativeRootDirectory = relativeRootDirectory;
    }

    public String getRelativeRootPath() {
        return relativeRootPath;
    }

    public void setRelativeRootPath(String relativeRootPath) {
        this.relativeRootPath = relativeRootPath;
    }

    @Override
    public void handle(RoutingContext context) {
        HttpServerRequest request = context.request();
        if (request.method() != HttpMethod.GET && request.method() != HttpMethod.HEAD) {
            context.next();
            return;
        }

        // decode URL path
        String uriDecodedPath = URIDecoder.decodeURIComponent(context.normalizedPath(), false);
        // if the normalized path is null it cannot be resolved
        if (uriDecodedPath == null) {
            context.next();
            return;
        }

        System.out.println(">>>>>>>>> uriDecodedPath = " + uriDecodedPath);

        // will normalize and handle all paths as UNIX paths
        String uriDecodedUnixPath = HttpUtils.removeDots(uriDecodedPath.replace('\\', '/'));
        uriDecodedUnixPath = Utils.pathOffset(uriDecodedUnixPath, context);
        if (uriDecodedUnixPath.startsWith("/")) {
            uriDecodedUnixPath = uriDecodedUnixPath.substring(1);
        }

        System.out.println(">>>>>>>>> uriDecodedUnixPath = " + uriDecodedUnixPath);

        System.out.println(">>>>>>>>> jarFileUri = " + jarFileUri);
        System.out.println(">>>>>>>>> relativeRootDirectory = " + relativeRootDirectory);
        System.out.println(">>>>>>>>> relativeRootPath = " + relativeRootPath);

        String requestedResource = uriDecodedPath
                .substring(uriDecodedPath.indexOf(relativeRootPath) + relativeRootPath.length());

        System.out.println(">>>>>>>>> requestedResource = " + requestedResource);

        if (requestedResource.endsWith("/")) {
            // looking for index...
            requestedResource = requestedResource + "index.html";
        }

        if (requestedResource.startsWith("/")) {
            requestedResource = requestedResource.substring(1);
        }
        URI jarFile = URI.create(jarFileUri);
        try {
            ZipUtils.copyFromZip(Path.of(jarFile), Path.of("/", "tmp", "poep"));
        } catch (IOException ex) {
            Logger.getLogger(DevUIStaticHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        //        try (FileSystem fileSystem = )) {
        //
        //
        //            Path requestedResourcePath = fileSystem.getPath(requestedResource);
        //            System.out.println(">>>>>> requestedResourcePath = " + requestedResourcePath);
        HttpServerResponse response = context.response();
        //response.headers().set(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE);
        //response.end(Buffer.buffer(Files.readAllBytes(requestedResourcePath)));
        response.end();
        //} catch (IOException ex) {
        //    Logger.getLogger(DevUIStaticHandler.class.getName()).log(Level.SEVERE, null, ex);
        //}

        //context.next();

    }

    /**
     * Resolves the web roots based on the webRootConfigurations
     *
     * @throws IOException if an I/O error occurs
     */
    //    private void resolveWebRoots() throws IOException {
    //        if (resolvedWebRoots == null) {
    //            try {
    //                ROOT_CREATION_LOCK.lock();
    //                if (resolvedWebRoots == null) {
    //                    List<Path> rootPaths = new ArrayList<>();
    //                    for (StaticWebRootConfiguration fsConfiguration : webRootConfigurations) {
    //                        Path sysFSPath = Paths.get(fsConfiguration.fileSystem);
    //                        Path rootPath = sysFSPath;
    //                        if (Files.isRegularFile(sysFSPath)) {
    //                            FileSystem fileSystem = ZipUtils.newFileSystem(sysFSPath);
    //                            rootPath = fileSystem.getPath("");
    //                        }
    //                        rootPaths.add(rootPath.resolve(fsConfiguration.webRoot));
    //                    }
    //                    resolvedWebRoots = new ArrayList<>(rootPaths);
    //                }
    //            } finally {
    //                ROOT_CREATION_LOCK.unlock();
    //            }
    //        }
    //    }

    /**
     *
     * @param context
     * @param path
     * @throws IOException if an I/O error occurs
     */
    //    private void sendStatic(RoutingContext context, String path) throws IOException {
    //        resolveWebRoots();
    //
    //        Path found = null;
    //        for (Path root : resolvedWebRoots) {
    //            Path resolved = root.resolve(path);
    //            if (Files.exists(resolved)) {
    //                found = resolved;
    //                break;
    //            }
    //        }
    //
    //        if (found == null) {
    //            context.next();
    //            return;
    //        }
    //
    //        final HttpServerResponse response = context.response();
    //        String contentType = MimeMapping.getMimeTypeForFilename(path);
    //        if (contentType != null) {
    //            if (contentType.startsWith("text")) {
    //                response.putHeader(HttpHeaders.CONTENT_TYPE, contentType + ";charset=" + DEFAULT_CONTENT_ENCODING);
    //            } else {
    //                response.putHeader(HttpHeaders.CONTENT_TYPE, contentType);
    //            }
    //        }
    //
    //        BasicFileAttributes fileAttributes = Files.readAttributes(found, BasicFileAttributes.class);
    //        if (fileAttributes.isDirectory()) {
    //            // directory listing is not supported
    //            context.next();
    //            return;
    //        } else if (fileAttributes.isRegularFile()) {
    //            context.end(Buffer.buffer(Files.readAllBytes(found)));
    //        }
    //    }
    //
    //    @Override
    //    public void close() throws IOException {
    //        if (resolvedWebRoots == null) {
    //            // web roots are not initialized, most likely no call to dev ui was made before
    //            return;
    //        }
    //
    //        // Close all filesystems that might have been created to access jar files
    //        for (Path resolvedWebRoot : resolvedWebRoots) {
    //            FileSystem fs = resolvedWebRoot.getFileSystem();
    //
    //            // never attempt to close the default filesystem, e.g. WindowsFileSystem. Only one instance of it exists.
    //            if (fs != FileSystems.getDefault()) {
    //                fs.close();
    //            }
    //        }
    //    }

}
