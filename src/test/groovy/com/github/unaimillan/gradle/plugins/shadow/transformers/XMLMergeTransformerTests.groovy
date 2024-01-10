package com.github.unaimillan.gradle.plugins.shadow.transformers

import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import org.codehaus.plexus.util.IOUtil
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLAssert
import org.custommonkey.xmlunit.XMLUnit
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import shadow.org.jdom2.IllegalAddException

import java.util.stream.Stream

import static org.junit.jupiter.api.DynamicTest.dynamicTest

class XMLMergeTransformerTests {

    private List<String> getResourceFiles(String path) throws IOException {
        List<String> filenames = new ArrayList<>()

        def pathStream = getClass().getResourceAsStream(path)
        BufferedReader br = new BufferedReader(new InputStreamReader(pathStream))
        String resource
        while ((resource = br.readLine()) != null) {
            filenames.add(resource)
        }
        return filenames
    }

    @TestFactory
    @DisplayName('XML Merging Tests')
    Stream<DynamicTest> dynamicTestsFromStreamFactoryMethodWithNames() {
        def root = '/tests'

        List<String> testUrls = getResourceFiles(root)

        // Returns a stream of dynamic tests.
        return testUrls.stream().map(testUrl ->
                dynamicTest('Testsuite ' + testUrl, () -> runTest(root + '/' + testUrl)))
    }

    void runTest(String testFolder) {
        def inputFiles = getResourceFiles(testFolder).stream().filter {
//            startsWith('input') && it.endsWith('.xml')
            it.matches('input\\d+.xml')
        }.toList()
        def input1 = testFolder + '/input1.xml'
        def expected = testFolder + '/expected.xml'

        XMLMergeTransformer transformer = new XMLMergeTransformer()

        XMLUnit.setNormalizeWhitespace(true)

        inputFiles.forEach(inputFile -> {
            def inputPath = testFolder + '/' + inputFile

//            try {
                transformer.transform(new TransformerContext(
                        input1,
                        getClass().getResourceAsStream(inputPath)
                ))
//            }catch (ex){
//                println ex
//            }
        })

//        transformer.transform(new TransformerContext(
//                input1,
//                getClass().getResourceAsStream(input1)
//        ))

        def expectedRes = IOUtil.toString(getClass().getResourceAsStream(expected), "UTF-8")
        def mergeRes = IOUtil.toString(transformer.getTransformedResource(), "UTF-8")

        Diff diff = XMLUnit.compareXML(expectedRes, mergeRes)
        XMLAssert.assertXMLIdentical(diff, true)
    }
}
