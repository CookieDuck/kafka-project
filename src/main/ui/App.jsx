import React, { Component } from 'react';
import ReactDOM from 'react-dom';
import NumericInput from 'react-numeric-input';
import {default as Select} from 'react-select';
import { SelectWrapper } from './components/App.styled';
import Cards from './components/Cards';
import bicycle from './decks/bicycle-ndo.json';
import chased from './decks/chased-ndo.json';
import redFirst from './decks/red-first.json';
import blackFirst from './decks/black-first.json';

const MIN_SHUFFLES = 1;
const MAX_SHUFFLES = 100;
const DECK_OPTIONS = [
  { value: 'bicycle', label: 'Bicycle', mapping: bicycle },
  { value: 'chased', label: 'Chased', mapping: chased },
  { value: 'red-first', label: 'Red first', mapping: redFirst },
  { value: 'black-first', label: 'Black first', mapping: blackFirst },
];

class App extends Component {
  constructor(props) {
    super(props);

    this.state = {
      timesToShuffle: 7,
      selectedDeck: DECK_OPTIONS[0],
      shuffled: [],
    };

    this.reconnect = this.reconnect.bind(this);
    this.disconnect = this.disconnect.bind(this);
    this.connectToServerSentEvents = this.connectToServerSentEvents.bind(this);
    this.sendShuffleRequest = this.sendShuffleRequest.bind(this);
    this.getSelectedDeck = this.getSelectedDeck.bind(this);

    this.handleMessage = this.handleMessage.bind(this);
    this.handleTimesToShuffleUpdate = this.handleTimesToShuffleUpdate.bind(this);
    this.handleInputDeckChange = this.handleInputDeckChange.bind(this);

    this.renderShuffleInputs = this.renderShuffleInputs.bind(this);
    this.renderOutput = this.renderOutput.bind(this);
    this.renderSSEButtons = this.renderSSEButtons.bind(this);

    this.connectToServerSentEvents();
  }

  componentWillUnmount() {
    this.disconnect();
  }

  connectToServerSentEvents() {
    this.sse = new EventSource('http://localhost:8080/shuffled');
    this.sse.onopen = evt => {
      console.debug("Connected to server sent events", evt);
    };
    this.sse.onmessage = this.handleMessage;
    this.sse.onerror = evt => {
      console.error("Got a problem, disconnecting", evt);
      this.disconnect();
    };
  }

  reconnect() {
    this.disconnect();
    this.connectToServerSentEvents();
  }

  disconnect() {
    this.sse.close();
  }

  sendShuffleRequest() {
    const data = {
      times: this.state.timesToShuffle,
      cards: this.getSelectedDeck(),
    };
    console.log('sending', data);
    fetch('http://localhost:8080/cards/shuffle', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
      body: JSON.stringify(data),
    })
      .then((data) => {
        console.debug('Success:', data);
      })
      .catch((error) => {
        console.error('Error:', error);
      });
  }

  getSelectedDeck() {
    const selectedDeck = this.state.selectedDeck || DECK_OPTIONS[0];
    return selectedDeck.mapping;
  }

  handleMessage(event) {
    if (event && event.data) {
      const data = JSON.parse(event.data);
      if (data && data.cards) {
        this.setState({ shuffled: data.cards });
      }
    }
  }

  handleTimesToShuffleUpdate(asNumber) {
    const timesToShuffle = Math.min(
      MAX_SHUFFLES,
      Math.max(asNumber, MIN_SHUFFLES)
    );
    console.log('update to', timesToShuffle);
    this.setState({ timesToShuffle });
  };

  handleInputDeckChange(selectedDeck) {
    this.setState({
      selectedDeck,
      shuffled: [],
    });
  }

  renderShuffleInputs() {
    const styles = {
      container: base => ({
        ...base,
        flex: 1
      })
    };

    return(
      <>
        <h1>Shuffle Some Cards</h1>
        <label>
          Number of times to shuffle:
          <NumericInput
            min={MIN_SHUFFLES}
            max={MAX_SHUFFLES}
            value={this.state.timesToShuffle}
            onChange={this.handleTimesToShuffleUpdate}
            style={{
              wrap: {
                'margin-left': '10px',
              },
              input: {
                'width': '60px',
              }
            }}
          />
        </label>
        <br/>
        <label>
          Deck to shuffle:
          <SelectWrapper>
            <Select
              defaultValue={this.state.selectedDeck}
              options={DECK_OPTIONS}
              onChange={this.handleInputDeckChange}
              styles={styles}
            />
          </SelectWrapper>
        </label>
        <Cards cards={this.getSelectedDeck()} />
        <br/>
        <button onClick={this.sendShuffleRequest} >
          Shuffle!
        </button>
      </>
    );
  }

  renderOutput() {
    return(
      this.state.shuffled.length > 0 ?
        <>
          <h2>Results:</h2>
          <Cards cards={this.state.shuffled}/>
        </> :
        <>
          <h2>Click the Shuffle button to see the results</h2>
        </>
    );
  }

  renderSSEButtons() {
    return(
      <>
        <h3>Server Sent Event controls</h3>
        <button onClick={this.reconnect} >
          Re-subscribe to events
        </button>
        <button onClick={this.disconnect} >
          Stop listening to events
        </button>
      </>
    );
  }

  render() {
    return (
      <div>
        {this.renderShuffleInputs()}
        {this.renderOutput()}
        <br/>
        {this.renderSSEButtons()}
      </div>
    );
  }
}

ReactDOM.render(
  <App />,
  document.getElementById('mount-point')
);
