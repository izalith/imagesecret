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
import {Formik, Form, Field} from "formik";
import 'assets/stylus/main.styl';
import Button from "react-bootstrap/Button";
import ToggleButtonGroup from "react-bootstrap/ToggleButtonGroup";
import ToggleButton from "react-bootstrap/ToggleButton";
import FormText from "react-bootstrap/FormText";

export default class ImageEncodeForm extends React.Component {
    render() {
        return (
            <div>
                <Formik
                    initialValues={{
                        payload: "",
                        payloadType: "text",
                        carrierImageFile: null,
                        payloadImageFile: null
                    }}
                    validate={values => {
                        let errors = {};
                        if (values.carrierImageFile == null) {
                            errors.carrierImageFile = "Carrier image is required";
                        }
                        if (values.payloadType === 'text' && !values.payload) {
                            errors.payload = "Payload text is required";
                        }
                        if (values.payloadType === 'file' && values.payloadImageFile == null) {
                            errors.payloadImageFile = "Payload file is required";
                        }
                        return errors;
                    }}
                    onSubmit={this.props.sendEncodeRequest}
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

                        let errorPayloadRequired = values.payloadType === 'text' && touched.payload && errors.payload;
                        let errorCarrierRequired = touched.carrierImageFile && errors.carrierImageFile;
                        let errorPayloadFileRequired = values.payloadType === 'file' && touched.payloadImageFile && errors.payloadImageFile;
                        return (
                            <Form>
                                <legend>Embedding in image</legend>
                                <div className="form-group">
                                    <label htmlFor="file">Select .png or .jpeg image to use as a container</label>
                                    <input id="file" name="carrierImageFile" type="file" onChange={(event) => {
                                        setFieldValue("carrierImageFile", event.currentTarget.files[0]);
                                        this.props.sendGetPayloadCapacityRequest(event.currentTarget.files[0]);
                                    }} className={"form-control-file " + (errorCarrierRequired ? "is-invalid" : "")}/>
                                    {errorCarrierRequired ?
                                        <div className={"invalid-feedback"}>{errors.carrierImageFile}</div> : null}
                                    <div className={!!this.props.payloadCapacity ? '' : 'hidden'}>
                                        <FormText className="text-muted">
                                            File capacity: {this.props.payloadCapacity} bytes
                                        </FormText>
                                    </div>

                                </div>

                                <div className="form-group">
                                    <div>
                                        <label htmlFor="payloadType">Select the type of embedded data</label>
                                        <div><ToggleButtonGroup className="mt-3" name="payloadType" id="payloadType"
                                                                defaultValue={"text"}
                                                                onChange={(value) => {
                                                                    setFieldValue("payloadType", value)
                                                                }}>
                                            <ToggleButton variant="light" type="radio" name="payloadType" defaultChecked
                                                          value="text">
                                                text
                                            </ToggleButton>
                                            <ToggleButton variant="light" type="radio" name="payloadType" value="file">
                                                file
                                            </ToggleButton>
                                        </ToggleButtonGroup>
                                        </div>
                                    </div>

                                </div>

                                <div className={values.payloadType === 'text' ? 'hidden' : ''}>
                                    <div className="form-group">
                                        <label htmlFor="file">Select any file to embed</label>

                                        <input id="file" name="payloadImageFile" type="file" onChange={(event) => {
                                            setFieldValue("payloadImageFile", event.currentTarget.files[0]);
                                        }}
                                               className={"form-control-file " + (errorPayloadFileRequired ? "is-invalid" : "")}
                                               disabled={values.payloadType === 'text'}/>
                                        {errorPayloadFileRequired ?
                                            <div className={"invalid-feedback"}>{errors.payloadImageFile}</div> : null}
                                    </div>
                                </div>

                                <div className={values.payloadType !== 'text' ? 'hidden' : ''}>
                                    <div className="form-group">
                                        <label htmlFor="payload">Text to hide</label>
                                        <Field
                                            component="textarea"
                                            name="payload"
                                            placeholder="Enter the text to hide"
                                            className={"form-control " + (errorPayloadRequired ? "is-invalid" : "")}
                                        />
                                        {errorPayloadRequired ?
                                            <div className={"invalid-feedback"}>{errors.payload}</div> : null}
                                    </div>
                                </div>

                                <Button
                                    type="submit"
                                    className="btn btn-primary"
                                    disabled={isSubmitting}
                                >
                                    {isSubmitting ? "Please wait..." : "Embed"}
                                </Button>
                            </Form>
                        )
                    }}
                </Formik>
            </div>
        );
    }
}
