package com.sabre.schemas.utils;

import java.io.File;
import java.net.URL;

import org.osgi.framework.Version;

import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLLibraryStatus;
import com.sabre.schemacompiler.saver.LibraryModelSaver;
import com.sabre.schemacompiler.saver.LibrarySaveException;
import com.sabre.schemacompiler.util.URLUtils;
import com.sabre.schemas.controllers.ProjectController;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.ProjectNode;

public class LibraryNodeBuilder {

    public static LibraryNodeBuilder create(String name, String namespace, String prefix,
            Version version) {
        return new LibraryNodeBuilder(name, namespace, prefix, version);
    }

    private final TLLibrary tlLib;

    private LibraryNodeBuilder(String name, String namespace, String prefix, Version version) {
        tlLib = new TLLibrary();
        tlLib.setName(name);
        tlLib.setNamespaceAndVersion(namespace, version.toString());
        tlLib.setPrefix(prefix);
    }

    public LibraryNode build(ProjectNode testProject, ProjectController pc)
            throws LibrarySaveException {
        URL libURL = URLUtils.toURL(new File(testProject.getProject().getProjectFile()
                .getParentFile(), tlLib.getName() + ".otm"));
        tlLib.setLibraryUrl(libURL);
        new LibraryModelSaver().saveLibrary(tlLib);
        return pc.add(testProject, tlLib);
    }

    public LibraryNodeBuilder makeFinal() {
        tlLib.setStatus(TLLibraryStatus.FINAL);
        return this;
    }

}