/*
 * Copyright 2016 SimplifyOps, Inc. (http://simplifyops.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtolabs.rundeck.app.support

import groovy.xml.MarkupBuilder
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by greg on 10/16/15.
 */
class BuilderUtilSpec extends Specification {

    @Unroll
    def "element name with invalid chars"() {
        given:
            final StringWriter writer = new StringWriter()
            def builder = new MarkupBuilder(new IndentPrinter(new PrintWriter(writer), "", false))
            def bu = new BuilderUtil()

            def map = [(key): value]

        when:
            bu.objToDom('test', map, builder)
            final String result = writer.toString()

        then:
            result == expected

        where:
            key     | value       | expected
            'asdf'  | 'zomething' | '<test><asdf>zomething</asdf></test>'
            'as/df' | 'zomething' | '<test><element name=\'as/df\'>zomething</element></test>'

    }


    @Unroll
    def "data to dom"() {
        given:
            final StringWriter writer = new StringWriter()
            def builder = new MarkupBuilder(new IndentPrinter(new PrintWriter(writer), "", false))
            def bu = new BuilderUtil()

        when:
            bu.dataObjToDom(data, builder)
            final String result = writer.toString()

        then:
            result == expected

        where:
            data                           | expected
            'asdf'                         | '<value>asdf</value>'
            'as<df'                        | '<value>as&lt;df</value>'
            'as\ndf'                       | '<value><![CDATA[as\ndf]]></value>'
            [a: 'b']                       | '<map><value key=\'a\'>b</value></map>'
            [a: 'b', c: 'd']               | '<map><value key=\'a\'>b</value><value key=\'c\'>d</value></map>'
            ['a', 'b']                     | '<list><value>a</value><value>b</value></list>'
            ['a', 'b'].toSet()             | '<set><value>a</value><value>b</value></set>'
            [[a: 'b'], ['b'].toSet(), 'c'] |
            '<list><map><value key=\'a\'>b</value></map><set><value>b</value></set><value>c</value></list>'
    }

    @Unroll
    def "decode data"() {
        given:
            final StringWriter writer = new StringWriter()
            def builder = new MarkupBuilder(new IndentPrinter(new PrintWriter(writer), "", false))
            def bu = new BuilderUtil()

        when:
            bu.dataObjToDom(data, builder)
            final String result = writer.toString()

        then:
            result == expected

        where:
            data               | expected
            'asdf'             | '<value>asdf</value>'
            'as<df'            | '<value>as&lt;df</value>'
            'as\ndf'           | '<value><![CDATA[as\ndf]]></value>'
            [a: 'b']           | '<map><value key=\'a\'>b</value></map>'
            ['a', 'b']         | '<list><value>a</value><value>b</value></list>'
            ['a', 'b'].toSet() | '<set><value>a</value><value>b</value></set>'
    }

    @Unroll
    def "multiline strings output with original or forced line endings"() {
        given:
        final StringWriter writer = new StringWriter()
        def builder = new MarkupBuilder(new IndentPrinter(new PrintWriter(writer), "", false))
        def bu = new BuilderUtil()
        bu.forceLineEndings = force
        bu.lineEndingChars = chars
        bu.automaticMultilineCdata = false

        def map = [(key): string]

        when:
        bu.objToDom('test', map, builder)
        final String result = writer.toString()

        then:
        result == expected

        where:
        force | chars  | key           | string              | expected
        true  | '\n'   | 'data<cdata>' | 'abc def ghi'       | '<test><data><![CDATA[abc def ghi]]></data></test>'
        true  | '\n'   | 'data<cdata>' | 'abc\rdef\rghi'     | '<test><data><![CDATA[abc\ndef\nghi]]></data></test>'
        true  | '\n'   | 'data<cdata>' | 'abc\ndef\nghi'     | '<test><data><![CDATA[abc\ndef\nghi]]></data></test>'
        true  | '\n'   | 'data<cdata>' | 'abc\r\ndef\r\nghi' | '<test><data><![CDATA[abc\ndef\nghi]]></data></test>'
        true  | '\n'   | 'data'        | 'abc def ghi'       | '<test><data>abc def ghi</data></test>'
        true  | '\n'   | 'data'        | 'abc\rdef\rghi'     | '<test><data>abc\ndef\nghi</data></test>'
        true  | '\n'   | 'data'        | 'abc\ndef\nghi'     | '<test><data>abc\ndef\nghi</data></test>'
        true  | '\n'   | 'data'        | 'abc\r\ndef\r\nghi' | '<test><data>abc\ndef\nghi</data></test>'

        true  | '\r'   | 'data<cdata>' | 'abc def ghi'       | '<test><data><![CDATA[abc def ghi]]></data></test>'
        true  | '\r'   | 'data<cdata>' | 'abc\rdef\rghi'     | '<test><data><![CDATA[abc\rdef\rghi]]></data></test>'
        true  | '\r'   | 'data<cdata>' | 'abc\ndef\nghi'     | '<test><data><![CDATA[abc\rdef\rghi]]></data></test>'
        true  | '\r'   | 'data<cdata>' | 'abc\r\ndef\r\nghi' | '<test><data><![CDATA[abc\rdef\rghi]]></data></test>'
        true  | '\r'   | 'data'        | 'abc def ghi'       | '<test><data>abc def ghi</data></test>'
        true  | '\r'   | 'data'        | 'abc\rdef\rghi'     | '<test><data>abc\rdef\rghi</data></test>'
        true  | '\r'   | 'data'        | 'abc\ndef\nghi'     | '<test><data>abc\rdef\rghi</data></test>'
        true  | '\r'   | 'data'        | 'abc\r\ndef\r\nghi' | '<test><data>abc\rdef\rghi</data></test>'

        true  | '\r\n' | 'data<cdata>' | 'abc def ghi'       | '<test><data><![CDATA[abc def ghi]]></data></test>'
        true  | '\r\n' | 'data<cdata>' | 'abc\rdef\rghi'     | '<test><data><![CDATA[abc\r\ndef\r\nghi]]></data></test>'
        true  | '\r\n' | 'data<cdata>' | 'abc\ndef\nghi'     | '<test><data><![CDATA[abc\r\ndef\r\nghi]]></data></test>'
        true  | '\r\n' | 'data<cdata>' | 'abc\r\ndef\r\nghi' | '<test><data><![CDATA[abc\r\ndef\r\nghi]]></data></test>'
        true  | '\r\n' | 'data'        | 'abc def ghi'       | '<test><data>abc def ghi</data></test>'
        true  | '\r\n' | 'data'        | 'abc\rdef\rghi'     | '<test><data>abc\r\ndef\r\nghi</data></test>'
        true  | '\r\n' | 'data'        | 'abc\ndef\nghi'     | '<test><data>abc\r\ndef\r\nghi</data></test>'
        true  | '\r\n' | 'data'        | 'abc\r\ndef\r\nghi' | '<test><data>abc\r\ndef\r\nghi</data></test>'

        false | '\n'   | 'data<cdata>' | 'abc def ghi'       | '<test><data><![CDATA[abc def ghi]]></data></test>'
        false | '\n'   | 'data<cdata>' | 'abc\rdef\rghi'     | '<test><data><![CDATA[abc\rdef\rghi]]></data></test>'
        false | '\n'   | 'data<cdata>' | 'abc\ndef\nghi'     | '<test><data><![CDATA[abc\ndef\nghi]]></data></test>'
        false | '\n'   | 'data<cdata>' | 'abc\r\ndef\r\nghi' | '<test><data><![CDATA[abc\r\ndef\r\nghi]]></data></test>'
        false | '\n'   | 'data'        | 'abc def ghi'       | '<test><data>abc def ghi</data></test>'
        false | '\n'   | 'data'        | 'abc\rdef\rghi'     | '<test><data>abc\rdef\rghi</data></test>'
        false | '\n'   | 'data'        | 'abc\ndef\nghi'     | '<test><data>abc\ndef\nghi</data></test>'
        false | '\n'   | 'data'        | 'abc\r\ndef\r\nghi' | '<test><data>abc\r\ndef\r\nghi</data></test>'
    }

    @Unroll
    def "multiline strings force CDATA"() {
        given:
        final StringWriter writer = new StringWriter()
        def builder = new MarkupBuilder(new IndentPrinter(new PrintWriter(writer), "", false))
        def bu = new BuilderUtil()
        bu.forceLineEndings = true
        bu.lineEndingChars = '\n'
        bu.automaticMultilineCdata = forcecdata

        def map = [(key): string]

        when:
        bu.objToDom('test', map, builder)
        final String result = writer.toString()

        then:
        result == expected

        where:
        forcecdata | key           | string              | expected
        true       | 'data<cdata>' | 'abc def ghi'       | '<test><data><![CDATA[abc def ghi]]></data></test>'
        true       | 'data<cdata>' | 'abc\rdef\rghi'     | '<test><data><![CDATA[abc\ndef\nghi]]></data></test>'
        true       | 'data<cdata>' | 'abc\ndef\nghi'     | '<test><data><![CDATA[abc\ndef\nghi]]></data></test>'
        true       | 'data<cdata>' | 'abc\r\ndef\r\nghi' | '<test><data><![CDATA[abc\ndef\nghi]]></data></test>'
        true       | 'data'        | 'abc def ghi'       | '<test><data>abc def ghi</data></test>'
        true       | 'data'        | 'abc\rdef\rghi'     | '<test><data><![CDATA[abc\ndef\nghi]]></data></test>'
        true       | 'data'        | 'abc\ndef\nghi'     | '<test><data><![CDATA[abc\ndef\nghi]]></data></test>'
        true       | 'data'        | 'abc\r\ndef\r\nghi' | '<test><data><![CDATA[abc\ndef\nghi]]></data></test>'
    }

    def "replace line endings"() {

        when:
        def result = BuilderUtil.replaceLineEndings(string, chars)

        then:
        result == expected

        where:
        chars  | string              | expected
        '\n'   | 'abc def ghi'       | 'abc def ghi'
        '\n'   | 'abc\rdef\rghi'     | 'abc\ndef\nghi'
        '\n'   | 'abc\ndef\nghi'     | 'abc\ndef\nghi'
        '\n'   | 'abc\r\ndef\r\nghi' | 'abc\ndef\nghi'

        '\r'   | 'abc def ghi'       | 'abc def ghi'
        '\r'   | 'abc\rdef\rghi'     | 'abc\rdef\rghi'
        '\r'   | 'abc\ndef\nghi'     | 'abc\rdef\rghi'
        '\r'   | 'abc\r\ndef\r\nghi' | 'abc\rdef\rghi'

        '\r\n' | 'abc def ghi'       | 'abc def ghi'
        '\r\n' | 'abc\rdef\rghi'     | 'abc\r\ndef\r\nghi'
        '\r\n' | 'abc\ndef\nghi'     | 'abc\r\ndef\r\nghi'
        '\r\n' | 'abc\r\ndef\r\nghi' | 'abc\r\ndef\r\nghi'

    }

    def "trimSpacesAndReplaceLineEndings"() {

        when:
        def result = BuilderUtil.trimAllLinesAndReplaceLineEndings(input,ending)

        then:
        result == expected

        where:
        ending  | input             | expected
        '\n'    | 'test \n trim '   | 'test\n trim'
        '\r\n'  | 'test \n trim '   | 'test\r\n trim'

    }
}
