import dash
from dash.dependencies import Input, Output
import dash_core_components as dcc
import dash_html_components as html
import pandas as pd
from components import Column, Header, Row
import plotly.graph_objs as go
import plotly.plotly as py
from datetime import datetime as dt
import json
from math import ceil

app = dash.Dash(__name__)
server = app.server

pharmacies = pd.read_csv("data/pharmacies.csv")
prescriptionOfDrugs = pd.read_csv("data/prescriptionofdrugs.csv")
prescriptions = pd.read_csv("data/prescriptions.csv")


# Standard Dash app code below
app.layout = html.Div(className='container', children=[
    Header("Pharma421"),
    html.Pre(id='selected-data'),

    Row(
        children = [
            html.Div(
                className="pretty_container six columns",
                children=[
                    dcc.Graph(
                        id="leftGraph",
                        figure={
                            "data": [
                                go.Scatter(
                                    x=['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
                                    y=[len([True for date in prescriptions["prescription_date"] if (dt.strptime(date, "%Y-%m-%d").month==i)]) for i in range(1, 13)]
                                )
                            ],
                            "layout": dict(
                                title="Monthly Prescription Count",
                                autosize=True,
                                margin=dict(
                                    l=45,
                                    r=45,
                                    b=45,
                                    t=55
                                ),
                                hovermode="closest",
                                plot_bgcolor="#F9F9F9",
                                paper_bgcolor="#F9F9F9",
                            )
                        }
                    )
                ]
            ),
            html.Div(
                className="pretty_container six columns",
                children=[
                    dcc.Graph(id="rightGraph")
                ]
            ),
        ]
    )

])



@app.callback(
    Output("rightGraph", "figure"),
    [Input("leftGraph", "relayoutData")])
def updateLeftGraph(relayoutData):

    labels = list(set(prescriptions["complaint"]))
    data=[
        {
            "type": "pie",
            "labels": labels,
            "values": [len(prescriptions[prescriptions["complaint"] == i]) for i in labels],
            "hoverinfo": "label+value+percent",
            "textinfo": "label+percent+name"

        }
    ]
    layout = dict(
        title="Complaints Breakdown",
        autosize=True,
        margin=dict(
            l=45,
            r=45,
            b=45,
            t=55
        ),
        hovermode="closest",
        plot_bgcolor="#F9F9F9",
        paper_bgcolor="#F9F9F9",
    )
    return dict(data=data, layout=layout)


if __name__ == '__main__':
    app.run_server(debug=True)
