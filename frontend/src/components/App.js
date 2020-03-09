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

import React, { Component } from "react";

import '../../assets/stylus/main.styl';
import 'bootstrap/dist/js/bootstrap.bundle';
import 'bootstrap/dist/js/bootstrap';
import Container from "react-bootstrap/Container";
import EncodeContainer from './EncodeContainer.js';
import DecodeContainer from './DecodeContainer';

export default class App extends Component {
    render() {
        return (
            <Container>
            <div className={'tabs-container'}>
                <ul className="nav nav-tabs" id="myTab" role="tablist">
                    <li className="nav-item">
                        <a className="nav-link active" id="embed-tab" data-toggle="tab" href="#embed" role="tab"
                           aria-controls="embed" aria-selected="true">Embed</a>
                    </li>
                    <li className="nav-item ">
                        <a className="nav-link" id="extract-tab" data-toggle="tab" href="#extract" role="tab"
                           aria-controls="extract" aria-selected="false">Extract</a>
                    </li>
                </ul>
                <div className="tab-content" id="myTabContent">
                    <div className="tab-pane fade show active" id="embed" role="tabpanel" aria-labelledby="embed-tab">
                        <EncodeContainer/>
                    </div>
                    <div className="tab-pane fade" id="extract" role="tabpanel" aria-labelledby="extract-tab">
                        <DecodeContainer/>
                    </div>
                </div>
            </div>

    </Container>
        );
    }
}
