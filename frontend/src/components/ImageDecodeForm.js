/*
 * Copyright 2020 Ilya Titovskiy
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import React from "react";
import {Formik, Form} from "formik";
import 'assets/stylus/main.styl';
import Container from "react-bootstrap/Container";
import "react-bootstrap/FormGroup";
import Button from "react-bootstrap/Button";

export default class ImageDecodeForm extends React.Component {
    render() {
        return (
            <Container>
                <Formik
                    initialValues={{carrierImageFile: null}}
                    validate={values => {
                        let errors = {};
                        if (values.carrierImageFile == null) {
                            errors.carrierImageFile = "Carrier image is required";
                        }
                        return errors;
                    }}
                    onSubmit={this.props.sendRequest}
                >
                    {({
                          handleSubmit,
                          setFieldValue,
                          setFieldTouched,
                          values,
                          errors,
                          touched,
                          isSubmitting
                      }) => {
                        let errorCarrierRequired = touched.carrierImageFile && errors.carrierImageFile;
                        return (
                            <Form className="">
                                <legend>Extracting from image</legend>
                                <div className="form-group">
                                    <label htmlFor="file">Select .png or .jpeg image with hidden content</label>
                                    <input id="file" name="carrierImageFile" type="file" onChange={(event) => {
                                        setFieldValue("carrierImageFile", event.currentTarget.files[0]);
                                    }} className={"form-control-file " + (errorCarrierRequired ? "is-invalid" : "")}/>
                                    {errorCarrierRequired ?
                                        <div className={"invalid-feedback"}>{errors.carrierImageFile}</div> : null}
                                </div>

                                <Button
                                    type="submit"
                                    className="btn btn-primary"
                                    disabled={isSubmitting}
                                >
                                    {isSubmitting ? "Please wait..." : "Extract"}
                                </Button>
                            </Form>
                        )
                    }}
                </Formik>
            </Container>
        );
    }
}
