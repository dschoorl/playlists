import { Track } from '@spotify/web-api-ts-sdk';

export type Song = {
  artist: string;
  title: string;
  match?: Track;
};
