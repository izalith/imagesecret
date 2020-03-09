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
import ImageDecodeForm from './ImageDecodeForm';
import axios from "axios";
import Container from "react-bootstrap/Container";
import DismissibleErrorAlert from "./DissmissibleErrorAlert";
import SuccessAlert from "./SuccessAlert";

const Buffer = require('buffer');
const FileDownload = require('js-file-download');

export default class DecodeContainer extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            decodedText: null,
            error: null
        };
    }

    initFormStateVal = {
        carrierImageFile: null
    };

    sendRequest(values, actions) {
        let fd = new FormData();
        fd.append('carrierImageFile', values.carrierImageFile);
        axios.post('/api/decode', fd, {
            responseType: 'arraybuffer',
            headers: {'Content-Type': 'multipart/form-data'}
        })
            .then((response) => {
                const contentTypeHeader = response.headers["content-type"];
                if (contentTypeHeader === "application/json") {
                    this.setState({
                        error: null,
                        decodedText: Buffer.Buffer.from(response.data, 'binary').toString()
                    });
                } else {
                    this.setState({error: null, decodedText: "Encrypted content is a file."});
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
                }
            })
            .catch((error) => {
                if (error && error.response) {
                    let apiError = JSON.parse(Buffer.Buffer.from(error.response.data, 'binary').toString());
                    this.setState({error: apiError.message, decodedText: null});
                } else {
                    this.setState({error: "Unknown server error", decodedText: null});
                }
            });
        actions.setSubmitting(false);
    };

    render() {
        return (
            <div>
                <Container>
                    <div className="form-bordered">
                        <ImageDecodeForm initialState={this.initFormStateVal}
                                         sendRequest={(values, actions) => this.sendRequest(values, actions)}/>
                    </div>
                    <div>
                        <SuccessAlert show={!!this.state.decodedText}
                                      decodedText={this.state.decodedText}
                                      header="Extracting successful"
                                      text={this.state.decodedText}/>
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
