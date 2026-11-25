// Sends guesses to the backend in order without the game waiting on them.
const BASE = '/api/quizzes';

export interface StartQuizResponse {
  sessionId: string;
  mapId: string;
  currentTarget: string;
  targets: string[];
}

let queue: Promise<void> = Promise.resolve();

async function post(
  path: string,
  body: unknown
): Promise<Response> {
  const response = await fetch(`${BASE}${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
    keepalive: true,
  });

  if (!response.ok) {
    throw new Error(
      `Gameplay request failed: ${response.status}`
    );
  }

  return response;
}

export async function startQuiz(
  mapId: string,
  userId: number | null
): Promise<StartQuizResponse> {
  const response = await post('', {
    mapId,
    userId,
  });

  return response.json() as Promise<StartQuizResponse>;
}

export function submitGuess(
  sessionId: string,
  selectedRegionId: string,
  clickedLat?: number,
  clickedLng?: number
): void {
  queue = queue
    .then(() =>
      post(`/${sessionId}/guesses`, {
        selectedRegionId,
        clickedLat,
        clickedLng,
      })
    )
    .then(
      () => undefined,
      (err) => console.error('Unable to send guess:', err)
    );
}
