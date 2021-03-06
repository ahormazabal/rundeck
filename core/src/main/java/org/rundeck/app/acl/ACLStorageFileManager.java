package org.rundeck.app.acl;

import com.dtolabs.rundeck.core.authorization.RuleSetValidation;
import com.dtolabs.rundeck.core.authorization.providers.BaseValidator;
import com.dtolabs.rundeck.core.authorization.providers.PolicyCollection;
import com.dtolabs.rundeck.core.authorization.providers.Validator;
import com.dtolabs.rundeck.core.storage.ResourceMeta;
import com.dtolabs.rundeck.core.storage.StorageManager;
import com.dtolabs.utils.Streams;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.rundeck.storage.api.Resource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Provides ACLFileManager backed by a StorageManager, does NOT implement Listener semantics
 */
@Builder
@RequiredArgsConstructor
public class ACLStorageFileManager
        implements ACLFileManager

{
    private final String prefix;
    private final StorageManager storage;
    @Getter private final BaseValidator validator;
    private final String pattern = ".*\\.aclpolicy";

    /**
     * List the system aclpolicy file names, not including the dir path
     */
    public List<String> listStoredPolicyFiles() {
        return storage
                .listDirPaths(prefix, pattern)
                .stream()
                .map((it) -> it.substring(prefix.length()))
                .collect(Collectors.toList());
    }

    /**
     * @param file name without path
     * @return true if the policy file with the given name exists
     */
    @Override
    public boolean existsPolicyFile(String file) {
        return storage.existsFileResource(prefix + file);
    }

    /**
     * @param fileName name of policy file, without path
     * @return text contents of the policy file
     */
    @Override
    public String getPolicyFileContents(String fileName) throws IOException {
        AclPolicyFile aclPolicy = getAclPolicy(fileName);
        if (aclPolicy == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Streams.copyStream(aclPolicy.getInputStream(), baos);
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Load content to output stream
     *
     * @param fileName name of policy file, without path
     * @return length of output
     */
    @Override
    public long loadPolicyFileContents(String fileName, OutputStream outputStream) throws IOException {
        if (!existsPolicyFile(fileName)) {
            return -1;
        }
        return storage.loadFileResource(prefix + fileName, outputStream);
    }

    @Override
    public AclPolicyFile getAclPolicy(final String fileName) {
        if (!existsPolicyFile(fileName)) {
            return null;
        }
        Resource<ResourceMeta> resource = storage.getFileResource(prefix + fileName);
        if (resource == null) {
            return null;
        }
        ResourceMeta file = resource.getContents();

        return new AclPolicyImpl(
                () -> {
                    try {
                        return file.getInputStream();
                    } catch (IOException e) {
                        return null;
                    }
                },
                file.getModificationTime(),
                file.getCreationTime(),
                fileName
        );
    }

    @RequiredArgsConstructor
    static class AclPolicyImpl
            implements AclPolicyFile
    {
        private final Supplier<InputStream> inputStream;

        @Override
        public InputStream getInputStream() {
            return inputStream.get();
        }

        @Getter private final Date modified;
        @Getter private final Date created;
        @Getter private final String name;
    }

    /**
     * Store a system policy file
     *
     * @param fileName name without path
     * @param fileText contents
     * @return size of bytes stored
     */
    @Override
    public long storePolicyFileContents(String fileName, String fileText) {
        byte[] bytes = fileText.getBytes(StandardCharsets.UTF_8);
        Resource<ResourceMeta> result = storage.writeFileResource(
                prefix + fileName,
                new ByteArrayInputStream(bytes),
                new HashMap<>()
        );
        return bytes.length;
    }
    /**
     * Store a system policy file
     *
     * @param fileName name without path
     * @param input input stream
     * @return size of bytes stored
     */
    @Override
    public long storePolicyFile(String fileName, InputStream input) {
        Resource<ResourceMeta> result = storage.writeFileResource(
                prefix + fileName,
                input,
                new HashMap<>()
        );
        return result.getContents().getContentLength();
    }

    /**
     * Delete a policy file
     *
     * @return true if successful
     */
    @Override
    public boolean deletePolicyFile(String fileName) {
        return storage.deleteFileResource(prefix + fileName);
    }
}
