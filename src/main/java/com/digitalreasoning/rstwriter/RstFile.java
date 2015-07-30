package com.digitalreasoning.rstwriter;

import com.digitalreasoning.rstwriter.bodyelement.Paragraph;

/**
 * Represents a fully constructed (ready to be written) reStructuredText file. Should be used in conjunction with
 * {@link FileWriter} to create the .rst file. An RstFile can contain any body elements(including directives),
 * headings, or transitions. The Builder class is necessary to create an RstFile and an instance of a builder can be
 * obtained by statically calling the {@code builder} method.
 */
public class RstFile {
    private ContentBase content;

    protected RstFile(ContentBase contentBase){
        this.content = contentBase;
        this.content.isFile();
    }

    /**
     * Writes the content (the final reStructuredText) of this RstFile to a single String object and returns it.
     * @return the reStructuredText generated by this RstFile.
     */
    public String write(){
       return content.write();
    }

    protected ContentBase getContentBase(){
        return content;
    }

    /**
     * Returns a builder for a new empty .rst file. The file will be named by the parameter {@code fileName}.
     * @param fileName the desired name of the file. The final name will be {@code fileName}.rst
     * @return a builder for an RstFile
     */
    public static Builder getBuilder(String fileName){
        return new Builder(fileName);
    }


    /**
     * The Builder class is used to instantiate an RstFile object. The Builder takes all of the content to be included
     * in the RstFile and stores it until {@code build()} is called. An instance of the Builder can be obtained via
     * the static {@code builder} method in RstFile or by means of the public constructor. All additions of elements will
     * be seen in-order: that is, the order in which elements are added is the order they will appear in the RstFile.
     * However, when using the {@code addDefinition} method, the definition added will appear after all content in the file.
     * To place a definition in a specific place, use the {@code addBodyElement} method.
     */
    public static class Builder {
        private ContentBase contentBase;

        /**
         * Public constructor for the Builder of an RstFile.
         * @param fileName the desired name of the .rst file
         */
        public Builder(String fileName){
            contentBase = new ContentBase(fileName, -1);
        }

        /**
         * Adds a paragraph to the content of this Builder, with optional {@link Inline} text support.
         * @param text The text of the paragraph
         * @param inlines optional inline markup options. See {@link Inline} for syntax help
         * @return this Builder with the paragraph added
         */
        public Builder addParagraph(String text, Inline... inlines){
            contentBase.add(new Paragraph(text, inlines));
            return this;
        }

        /**
         * Adds a body element to the content of this Builder
         * @param bodyElement the body element to be added (order of additions will be preserved in the RstFile)
         * @return this Builder with the body element added
         */
        public Builder addBodyElement(RstBodyElement bodyElement){
            contentBase.add(bodyElement);
            return this;
        }

        /**
         * Adds a directive to the content of this Builder. Directives are body elements, so they will be seen among other
         * elements in order of addition
         * @param directive the directive to be added
         * @return this Builder with the directive added
         */
        public Builder addDirective(Directive directive){
            contentBase.add(directive);
            return this;
        }

        /**
         * Adds a definition to the content of this Builder. The definition will be seen after all other content in the RstFile
         * @param definition the Definition to be added
         * @return this Builder with the definition added
         */
        public Builder addDefinition(Definition definition){
            contentBase.addDefinition(definition);
            return this;
        }

        /**
         * Adds a heading to the content of this Builder. Headings will be seen after body elements, in order of addition
         * @param heading the heading to be added
         * @return this Builder with the heading added
         */
        public Builder addHeading(Heading heading){
            contentBase.add(heading.getContentBase());
            return this;
        }

        /**
         * Adds a transition (a horizontal bar) to the content of this Builder. Transitions behave like body elements
         * in the RstFile's ordering of content
         * @return this Builder with a transition added
         */
        public Builder addTransition(){
            contentBase.add(new Transition());
            return this;
        }

        /**
         * Builds an RstFile object from the content provided to the Builder and returns it. {@code build} can be called multiple
         * times, and modifications to the Builder afterward will not be seen in any previous RstFiles.
         * @return a fully formed, unmodifiable RstFile
         */
        public RstFile build(){
            ContentBase copy = new ContentBase(contentBase);
            return new RstFile(copy);
        }
    }
}
