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

app = dash.Dash(__name__)
server = app.server

pharmacies = pd.read_csv("data/pharmacies.csv")
prescriptionOfDrugs = pd.read_csv("data/prescriptionofdrugs.csv")
prescriptions = pd.read_csv("data/prescriptions.csv")

layout = dict(
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
    legend=dict(font=dict(size=10), orientation='h'),
)

# Standard Dash app code below
app.layout = html.Div(className='container', children=[
    Header("Pharma421"),
    html.Pre(id='selected-data'),
    Row(
        style={
            "display": "flex"
        },
        children=[
            html.Div(
                style={
                    "flex" : "1"
                },
                className="pretty_container",
                children = [
                    html.P(""),
                    html.H6(
                        id="info1",
                        className="info_text"
                    )
                ]
            ),
            html.Div(
                style={
                    "flex" : "1"
                },
                className="pretty_container",
                children = [
                    html.P(""),
                    html.H6(
                        id="info2",
                        className="info_text"
                    )
                ]
            ),
            html.Div(
                style={
                    "flex" : "1"
                },
                className="pretty_container",
                children = [
                    html.P(""),
                    html.H6(
                        id="info3",
                        className="info_text"
                    )
                ]
            )
        ]
    ),
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
                            "layout": {
                                "title": "Monhtly Prescription Count"
                            }
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

def filterDf(start, end):
    start=dt.strptime(start, "%Y-%m/%d")
    end=dt.strptime(end, "%Y-%m-%d")
    return prescriptions[dt.strptime(prescriptions["prescription_date"], "%Y-%m-%d") > start
        and dt.strptime(prescriptions["prescription_date"], "%Y-%m-%d") < end]


# @app.callback(
#     Output("leftGraph", "figure"),
#     [Input("leftGraph", "selectedData")])
# def updateLeftGraph(selected_data):
#     data=[
#         {
#             "x": [i for i in range(1, 10)],
#             "y": [j for j in range(11, 20)]
#         }
#     ]
#     return dict(data=data)

@app.callback(
    Output("rightGraph", "figure"),
    [Input("leftGraph", "selectedData")])
def updateLeftGraph(selected_data):

    if selected_data:
        filtered=filterDf()
    else:
        filtered=prescriptions
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
    return dict(data=data, layout={"title": "Complaints Breakdown"})

if __name__ == '__main__':
    app.run_server(debug=True)
