package com.github.unaimillan.gradle.plugins.shadow.transformers

import com.github.jengelman.gradle.plugins.shadow.transformers.CacheableTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.Transformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext

import org.apache.tools.zip.ZipOutputStream
import org.apache.tools.zip.ZipEntry

import org.gradle.api.file.FileTreeElement
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.jdom2.Attribute
import org.jdom2.Content
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.JDOMException
import org.jdom2.input.SAXBuilder
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource
import org.xml.sax.SAXException

/**
 * Merges multiple occurrences of some XML file into one.
 * <p>
 * Modified from com.github.jengelman.gradle.plugins.shadow.transformers.XmlAppendingTransformer.groovy
 *
 * @author John Engelman
 */
@CacheableTransformer
class XMLMergeTransformer implements Transformer {
    public static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance"

    @Input
    boolean ignoreDtd = true

    @Optional
    @Input
    String resource

    private Document doc

    boolean canTransformResource(FileTreeElement element) {
        def path = element.relativePath.pathString
        if (resource != null && resource.equalsIgnoreCase(path)) {
            return true
        }

        return false
    }

    void transform(TransformerContext context) {
        Document r
        try {
            SAXBuilder builder = new SAXBuilder(false)
            builder.setExpandEntities(false)
            if (ignoreDtd) {
                builder.setEntityResolver(new EntityResolver() {
                    InputSource resolveEntity(String publicId, String systemId)
                            throws SAXException, IOException {
                        return new InputSource(new StringReader(""))
                    }
                })
            }
            r = builder.build(context.is)
        }
        catch (JDOMException e) {
            throw new RuntimeException("Error processing resource " + resource + ": " + e.getMessage(), e)
        }

        if (doc == null) {
            doc = r
        } else {
            Element root = r.getRootElement()

            root.attributes.each { Attribute a ->

                Element mergedEl = doc.getRootElement()
                Attribute mergedAtt = mergedEl.getAttribute(a.getName(), a.getNamespace())
                if (mergedAtt == null) {
                    Attribute attr = a.clone()
                    attr.detach()
                    mergedEl.setAttribute(attr)
                }
            }

            root.children.each { Content n ->
                Content content = n.clone()
                content.detach()
                doc.getRootElement().addContent(content)
            }
        }
    }

    boolean hasTransformedResource() {
        return doc != null
    }

    void modifyOutputStream(ZipOutputStream os, boolean preserveFileTimestamps) {
        ZipEntry entry = new ZipEntry(resource)
        entry.time = TransformerContext.getEntryTimestamp(preserveFileTimestamps, entry.time)
        os.putNextEntry(entry)
        new XMLOutputter(Format.getPrettyFormat()).output(doc, os)

        doc = null
    }

    byte[] getTransformedResource()
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 4)

        (new XMLOutputter(Format.getPrettyFormat())).output(doc, baos)

        return baos.toByteArray()
    }
}