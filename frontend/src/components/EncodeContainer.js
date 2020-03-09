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

import React from 'react';
import ImageEncodeForm from './ImageEncodeForm';
import axios from "axios";
import 'js-file-download';
import Container from "react-bootstrap/Container";
import DismissibleErrorAlert from "./DissmissibleErrorAlert";
import SuccessAlert from "./SuccessAlert";

const FileDownload = require('js-file-download');
const Buffer = require('buffer');

export default class EncodeContainer extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            payloadCapacity: null,
            error: null,
            success: null
        };
    }

    initFormStateVal = {
        payloadType: "text",
        carrierImageFile: null,
        payloadImageFile: null
    };

    sendEncodeRequest(values, actions) {
        let fd = new FormData();
        fd.append('carrierImageFile', values.carrierImageFile);
        let context;
        let contentType;
        if (values.payloadType === "text") {
            fd.append('payload', values.payload);
            context = "/api/encodeText";
            contentType = "text/html";
        } else {
            fd.append('payloadImageFile', values.payloadImageFile);
            context = "/api/encodeFile";
            contentType = "multipart/form-data";
        }


        axios.post(context, fd, {
            responseType: 'arraybuffer',
            headers: {
                'Content-Type': contentType
            }
        })
            .then((response) => {
                const dispositionHeader = response.headers["content-disposition"];
                let suggestedFileName;
                if (dispositionHeader) {
                    let filenameMatches = dispositionHeader.match("filename=\".*\"");
                    if (filenameMatches != null && filenameMatches.length === 1) {
                        suggestedFileName = filenameMatches[0].slice(10, -1);
                    }
                }
                const resultFilename = suggestedFileName ? suggestedFileName : values.sourceImageFile.name;
                FileDownload(response.data, resultFilename);
                this.setState({error: null, success: true});
            })
            .catch((error) => {
                if (error && error.response) {
                    let apiError = JSON.parse(Buffer.Buffer.from(error.response.data, 'binary').toString());
                    this.setState({error: apiError.message, success: null});
                } else {
                    this.setState({error: "Unknown server error", decodedText: null});
                }
            });
        actions.setSubmitting(false);
    };

    sendGetPayloadCapacityRequest(carrierImageFile) {
        let fd = new FormData();
        fd.append('carrierImageFile', carrierImageFile);
        axios.post("/api/getPayloadCapacity", fd, {
            headers: {
                'Content-Type': "multipart/form-data"
            }
        })
            .then((response) => {
                const contentTypeHeader = response.headers["content-type"];
                if (contentTypeHeader === "application/json") {
                    this.setState({payloadCapacity: response.data.payloadCapacity});
                }
            })
            .catch((error) => {
                let apiError = error.response.data;
                this.setState({error: apiError.message, success: null});
            });
    };

    render() {
        return (
            <div>
                <Container>
                    <div className="form-bordered">
                        <ImageEncodeForm initialState={this.initFormStateVal}
                                         sendEncodeRequest={(values, actions) => {
                                             this.sendEncodeRequest(values, actions);
                                         }}
                                         sendGetPayloadCapacityRequest={(file) =>
                                             this.sendGetPayloadCapacityRequest(file)
                                         }
                                         payloadCapacity={this.state.payloadCapacity}/>
                    </div>
                    <div>
                        <SuccessAlert show={!!this.state.success}
                                      header="Embedding successful"
                                      text="Resulting file is downloading"/>
                        <DismissibleErrorAlert errorText={this.state.error}
                                               show={!!this.state.error}
                                               hideError={() => {
                                                   this.setState({error: null});
                                               }}/>
                    </div>
                </Container>
            </div>

        );
    }

};